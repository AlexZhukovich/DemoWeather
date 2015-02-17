package com.alexzh.demoweather.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.alexzh.demoweather.R;
import com.alexzh.demoweather.data.WeatherContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class WeatherSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = WeatherSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static String mLatitude;
    private static String mLongitude;

    public WeatherSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonStr = null;

        String mUnits = "metric";

        try {
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast?";
            final String UNITS_PARAM = "units";
            final String LATITUDE_PARAM = "lat";
            final String LONGITUDE_PARAM = "lon";


            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(LATITUDE_PARAM, mLatitude)
                    .appendQueryParameter(LONGITUDE_PARAM, mLongitude)
                    .appendQueryParameter(UNITS_PARAM, mUnits)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            final String OWM_CITY_ID = "id";
            final String OWM_CITY = "city";
            final String OWM_CITY_NAME = "name";
            final String OWM_COORD = "coord";
            final String OWM_COORD_LAT = "lat";
            final String OWM_COORD_LONG = "lon";

            final String OWM_LIST = "list";

            final String OWM_DATETIME = "dt";
            final String OWM_PRESSURE = "pressure";
            final String OWM_HUMIDITY = "humidity";
            final String OWM_WINDSPEED = "speed";
            final String OWM_WIND_DIRECTION = "deg";

            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";

            final String OWM_WEATHER = "weather";
            final String OWM_DESCRIPTION = "main";
            final String OWN_WIND = "wind";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityId = cityJson.getString(OWM_CITY_ID);
            String cityName = cityJson.getString(OWM_CITY_NAME);
            JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = coordJSON.getDouble(OWM_COORD_LAT);
            double cityLongitude = coordJSON.getDouble(OWM_COORD_LONG);

            cityLatitude = new BigDecimal(cityLatitude).setScale(2, RoundingMode.UP).doubleValue();
            cityLongitude = new BigDecimal(cityLongitude).setScale(2, RoundingMode.UP).doubleValue();

            long locationID = addLocation(cityId, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            for(int i = 0; i < weatherArray.length(); i++) {
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                dateTime = dayForecast.getLong(OWM_DATETIME);

                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_DESCRIPTION);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                pressure = temperatureObject.getDouble(OWM_PRESSURE);
                humidity = temperatureObject.getInt(OWM_HUMIDITY);

                JSONObject windObject = dayForecast.getJSONObject(OWN_WIND);
                windSpeed = windObject.getDouble(OWM_WINDSPEED);
                windDirection = windObject.getDouble(OWM_WIND_DIRECTION);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOCATION_ID, locationID);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE,
                        WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, description);

                cVVector.add(weatherValues);
            }
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

                Calendar cal = Calendar.getInstance(); //Get's a calendar object with the current time.
                cal.add(Calendar.DATE, -1); //Signifies yesterday's date
                String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
                getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
                        new String[] {yesterdayDate});
            }
            getContext().getContentResolver().notifyChange(WeatherContract.WeatherEntry.buildDaysUri(), null, true);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    private long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor locationCursor = getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_CITY_ID + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_ID, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, lon);

            Uri insertedUri = getContext().getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        return locationId;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, String latitude, String longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */


        }
        return newAccount;
    }
}