package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BottomNavigationView bottomNavigationView;
    private ValueCallback<Uri[]> mUploadCallback;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // WebView 초기화
        webView = findViewById(R.id.webView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        WebSettings webSettings = webView.getSettings();

        // JavaScript 활성화
        webSettings.setJavaScriptEnabled(true);
        // WebViewClient 설정
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        // 런타임 권한 요청
        requestStoragePermission();

        // 파일 선택 다이얼로그 처리
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                openFileChooser(filePathCallback);
                return true;
            }
        });

        // 에러 핸들링 추가
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("WebView Error", "Error Code: " + errorCode + ", Description: " + description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 페이지 로딩이 완료된 후 JavaScript 함수 실행
                String jsCode = "javascript:(function() { " +
                        "resizeWebView();" +
                        "})()";
                webView.loadUrl(jsCode);

                // URL에 따라 BottomNavigationView를 조절
                if ("http://121.178.82.230/UserApp".equals(url)) {
                    bottomNavigationView.setVisibility(View.GONE); // 숨기기
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE); // 나타내기
                }
            }
        });

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void resizeWebView() {
                // Android에서 호출될 때 실행되는 코드
                Log.d("WebView", "resizeWebView 함수가 호출되었습니다.");
            }
        }, "Android");

        // WebView 크기 확인
        webView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int webViewWidth = webView.getWidth();
                int webViewHeight = webView.getHeight();
                Log.d("WebView", "WebView Width: " + webViewWidth + ", WebView Height: " + webViewHeight);
                // 리스너를 한 번 호출한 후에 제거
                webView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        // WebView 로드
        webView.loadUrl("http://121.138.145.75/UserAppMain");

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    // 홈 페이지로 이동
                    webView.loadUrl("http://121.138.145.75/UserAppMain");
                } else if (itemId == R.id.MainPage) {
                    // 마이페이지 페이지로 이동
                    webView.loadUrl("http://121.138.145.75/userAppRequestPayout");
                } else if (itemId == R.id.userAppPayOut) {
                    // 배분 내역 페이지로 이동
                    webView.loadUrl("http://121.138.145.75/userAppPayOut");
                } else if (itemId == R.id.userAppInvestment) {
                    // 투자 내역 페이지로 이동
                    webView.loadUrl("http://121.138.145.75/userAppInvestment");
                } else if (itemId == R.id.userAppRequestFund) {
                    // 송금 내역 페이지로 이동
                    webView.loadUrl("http://121.138.145.75/userAppRequestFund");
                } else {
                    // 기본 동작
                    return false;
                }
                return true;
            }
        });
    }

    // 파일 선택 다이얼로그 띄우기
    private void openFileChooser(ValueCallback<Uri[]> filePathCallback) {
        mUploadCallback = filePathCallback;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Ensure that there's a default application that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 123);
        } else {
            Log.e("FileUpload", "No application can handle the file picker intent.");
            if (mUploadCallback != null) {
                mUploadCallback.onReceiveValue(null);
                mUploadCallback = null;
            }
        }
    }

    // 런타임 권한 요청
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 사용자에게 권한 요청
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                // 이미 권한이 있는 경우에 대한 처리
                // 예: 웹뷰 초기화 등
            }
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인된 경우에 대한 처리
                // 예: 웹뷰 초기화 등
            } else {
                // 권한이 거부된 경우에 대한 처리
            }
        }
    }

    // 파일 선택 다이얼로그에서 선택한 파일 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (mUploadCallback != null) {
                    Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    mUploadCallback.onReceiveValue(result);
                    mUploadCallback = null;


                }
            } else {
                if (mUploadCallback != null) {
                    mUploadCallback.onReceiveValue(null);
                    mUploadCallback = null;
                }
            }
        }
        webView.clearCache(true);

    }
    private void refreshWebView() {
        webView.reload();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            // 웹뷰에서 이전 페이지로 이동 가능한 경우
            webView.goBack();
        } else {
            // 웹뷰에서 더 이상 이전 페이지로 이동할 수 없는 경우
            super.onBackPressed();
        }
    }
}

