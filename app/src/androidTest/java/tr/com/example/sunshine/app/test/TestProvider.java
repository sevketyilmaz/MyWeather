package tr.com.example.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import tr.com.example.sunshine.app.data.WeatherContract.LocationEntry;
import tr.com.example.sunshine.app.data.WeatherContract.WeatherEntry;
import tr.com.example.sunshine.app.data.WeatherDbHelper;

/**
 * Created by svkt on 16.08.2014.
 */
public class TestProvider extends AndroidTestCase{
    public static final String TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable{
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);

    }

    public void testInsertReadProvider(){

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestDb.createNorthPoleLocationContentValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        //Verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(TAG, "New row id: " + locationRowId);

        //In theory data is inserted. now we pull some out of it and check it
        //A CURSOR your primary interface to the query results
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, //Table to query
                null, //if its null, returns all columns
                null,   //columns for WHERE clause
                null,   //values for WHERE clause
                null,   //columns to GROUP BY
                null,   //columns to filter by row groups
                null    //sort order
        );

        TestDb.validateCursor(cursor, testValues);

        ContentValues weatherValues = TestDb.createWeatherContentValues(locationRowId);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
    }

    public void testGetType(){
        // content://tr.com.example.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/tr.com.example.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://tr.com.exapmle.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/tr.com.example.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://tr.com.example.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/tr.com.example.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);



        // content://tr.com.example.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://tr.com.example.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);

    }



}
