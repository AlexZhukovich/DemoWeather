package com.alexzh.demoweather;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import com.alexzh.demoweather.data.WeatherContract.LocationEntry;
import com.alexzh.demoweather.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertAndReadNewData() {
        ContentValues testValues = TestDb.createMinskLocationValue();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(cursor, testValues);

        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);


        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherUri(),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    public void testGetType() {
        // content://com.alexzh.demoweather/location/
        String type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.alexzh.demoweather/weather/
        type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals(WeatherEntry.CONTENT_TYPE, type);
    }



}
