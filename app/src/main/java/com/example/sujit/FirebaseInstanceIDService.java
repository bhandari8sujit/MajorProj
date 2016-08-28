package com.example.sujit.customerapp;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token= FirebaseInstanceId.getInstance().getToken();

        SharedPreferences sharedPreference=getSharedPreferences("DeviceToken",MODE_PRIVATE);
        SharedPreferences.Editor spEditor= sharedPreference.edit();
        spEditor.putString("token",token);
        spEditor.apply();

        //registerToken(token);

    }
/*
    private void registerToken(String token) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",token)
                .build();

        Request request = new Request.Builder()
                .url("http://3aba37d5.ngrok.io/tokentemp")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
