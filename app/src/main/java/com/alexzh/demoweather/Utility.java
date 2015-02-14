package com.alexzh.demoweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Utility {

    private final static String CLOUDS_WEATHER = "Clouds";
    private final static String CLEAR_WEATHER = "Clear";
    private final static String RAIN_WEATHER = "Rain";

    public static String getUnits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric));
    }

    public static boolean isInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static String getWeatherImageUrl(String weather) {
        switch (weather) {
            case CLOUDS_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/cloudy.png";
            case CLEAR_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/sunny.png";
            case RAIN_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/rain.png";

        }
        return null;
    }

}
