package com.alexzh.demoweather.data;

import android.provider.BaseColumns;

public class WeatherContract {

    public final static class LocationEntry implements BaseColumns {

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_CITY_NAME = "city_name";

        public static final String COLUMN_LONGITUDE = "longitude";

        public static final String COLUMN_LATITUDE = "latitude";

    }

    public final static class WeatherEntry implements BaseColumns {

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
    }
}
