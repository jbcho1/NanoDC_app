package com.example.myapplication;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        Log.d("FCM Log", "Refreshed token: " + token);
        // 토큰이 갱신될 때 실행되는 코드를 추가하세요.
    }
}