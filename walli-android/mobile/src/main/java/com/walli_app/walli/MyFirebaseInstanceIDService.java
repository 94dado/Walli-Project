package com.walli_app.walli;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by dado on 08/07/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    //in realtà è codice inutile, ma lo lascio per sicurezza
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("token",token);
        edit.apply();
    }
}
