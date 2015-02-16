package com.alexzh.demoweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WeatherSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static WeatherSyncAdapter sWeatherSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("WeatherSyncService", "onCreate - WeatherSyncService");
        synchronized (sSyncAdapterLock) {
            if (sWeatherSyncAdapter == null) {
                sWeatherSyncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sWeatherSyncAdapter.getSyncAdapterBinder();
    }
}