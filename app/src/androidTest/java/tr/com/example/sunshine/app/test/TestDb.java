package tr.com.example.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import tr.com.example.sunshine.app.data.WeatherContract.LocationEntry;
import tr.com.example.sunshine.app.data.WeatherDbHelper;

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

    }

}
