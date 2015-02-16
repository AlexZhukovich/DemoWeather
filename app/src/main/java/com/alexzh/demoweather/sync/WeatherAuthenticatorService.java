package com.alexzh.demoweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WeatherAuthenticatorService extends Service {

    private WeatherAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new WeatherAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
