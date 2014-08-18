package tr.com.example.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import tr.com.example.sunshine.app.data.WeatherDbHelper;
import tr.com.example.sunshine.app.data.WeatherContract.*;

/**
 * Created by svkt on 16.08.2014.
 */
public class TestDb extends AndroidTestCase{
    public static final String TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable{
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        db.close();
    }

    public void testInsertReadDb(){

        //Test data we'r going to insert into the DB to see if it works
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        //Verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(TAG, "New row id: " + locationRowId);

        //In theory data is inserted. now we pull some out of it and check it
        //Specify which columns you want
        String[] columns = {
          LocationEntry._ID,
          LocationEntry.COLUMN_LOCATION_SETTING,
          LocationEntry.COLUMN_CITY_NAME,
          LocationEntry.COLUMN_COORD_LAT,
          LocationEntry.COLUMN_COORD_LONG
        };

        //A CURSOR your primary interface to the query results
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, //Table to query
                columns,
                null,   //columns for WHERE clause
                null,   //values for WHERE clause
                null,   //columns to GROUP BY
                null,   //columns to filter by row groups
                null    //sort order
        );

        //if possible move to the first row of the query results.
        if(cursor.moveToFirst()){
            //Get the value of each column by finding the appropriate column index
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int cityNameIndex = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String cityName = cursor.getString(cityNameIndex);

            int latIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            double longitude = cursor.getDouble(longIndex);

            assertEquals(testCityName, cityName);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

        }else {
            fail("no values returned :(/");
        }

        /*Now do the things for weather table*/
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        if (!weatherCursor.moveToFirst()) {
            fail("No weather data returned!");
        }

        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)), locationRowId);
        assertEquals(weatherCursor.getString(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)), "20141205");
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)), 1.1);
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)), 1.2);
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)), 1.3);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), 75);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), 65);
        assertEquals(weatherCursor.getString(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)), "Asteroids");
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)), 5.5);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)), 321);

        weatherCursor.close();
        dbHelper.close();
    }

}
