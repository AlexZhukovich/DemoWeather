package com.alexzh.demoweather.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "com.alexzh.demoweather";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DAYS = "days";
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String DATE_TIME_FORMAT = "yyyyMMdd HH:mm";

    public static String getDbDateString(Date date){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        return sdf.format(date);
    }

    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public final static class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_CITY_ID = "city_id";

        public static final String COLUMN_CITY_NAME = "city_name";

        public static final String COLUMN_LONGITUDE = "longitude";

        public static final String COLUMN_LATITUDE = "latitude";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildLocationUri() {
            return CONTENT_URI;
        }


    }

    public final static class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final Uri DAYS_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAYS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";

        public static final String COLUMN_LOCATION_ID = "location_id";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_MIN_TEMPERATURE = "min_temp";

        public static final String COLUMN_MAX_TEMPERATURE = "max_temp";

        public static final String COLUMN_HUMIDITY = "humidity";

        public static final String COLUMN_PRESSURE = "pressure";

        public static final String COLUMN_WIND = "wind";

        public static final String COLUMN_DEGREES = "degrees";

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDaysUri() {
            return DAYS_CONTENT_URI;
        }

        public static Uri buildWeatherUri() {
            return CONTENT_URI;
        }

        public static Uri buildWeatherLocationWithDate(long date, String latitude, String longitude) {
            return CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(date))
                    .appendPath(latitude)
                    .appendPath(longitude)
                    .build();
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getLatitudeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getLongitudeFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }
    }
}
