package com.alexzh.demoweather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import com.alexzh.demoweather.data.WeatherContract;
import com.alexzh.demoweather.data.WeatherDbHelper;
import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertAndReadToDb() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createMinskLocationValue();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        validateCursor(cursor, testValues);

        ContentValues weatherValues = createWeatherValues(locationRowId);

        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        Cursor weatherCursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
    }

    public ContentValues createMinskLocationValue() {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "Minsk");
        values.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, "53.901");
        values.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, "27.5667");
        return values;
    }

    public ContentValues createWeatherValues(long locationId) {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.WeatherEntry.COLUMN_LOCATION_ID, locationId);
        values.put(WeatherContract.WeatherEntry.COLUMN_DATE, 1423926000);
        values.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, "Clear");
        values.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE, "-2");
        values.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE, "2");
        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, "1.2");
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, "1.7");
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND, "2.3");
        values.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, "1.1");
        return values;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}
