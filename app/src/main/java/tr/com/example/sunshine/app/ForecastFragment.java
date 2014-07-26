package tr.com.example.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by svkt on 26.07.2014.
 */
public class ForecastFragment extends Fragment {
    private static final String TAG = ForecastFragment.class.getSimpleName();

    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add this line in order for this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {
                "Today - Sunny - 89/78",
                "Tomorrow - Foggy - 12/85",
                "Wednesday - Cloudy - */*",
                "Thursday - Asteroids - */*",
                "Friday - Rain - */*",
                "Pazar - acik - */*"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                Log.e(TAG,"refresh clicked");
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                weatherTask.execute("94043");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            //if there is no zipcode, nothing to look
            if(params.length == 0)
                return null;

            // These two need to be declared outside the try/catch so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(TAG,"Built uri: " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(TAG, "forecastJSONstr: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                e.printStackTrace();
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch (JSONException e){
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            //this will only happen if there was an error getting or parsing the forecast
            return null;
        }

        /**
         * Take the String representating the complete forecast in JSon format and pull out the data
         * we need to construct the Strings needed for the wireframes
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws  JSONException{
            //These are the names of the JSON objects that need to be extracted
            final String OWN_LIST = "list";
            final String OWN_WEATHER = "weather";
            final String OWN_TEMPERATURE = "temp";
            final String OWN_MAX = "max";
            final String OWN_MIN = "min";
            final String OWN_DATETIME = "dt";
            final String OWN_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWN_LIST);

            String[] resultStrs = new String[numDays];

            //walk in weather array for each object for representing the day
            for(int i = 0; i < weatherArray.length(); i++){
                //Used format "Day, Description, Hi/Low"
                String day;
                String description;
                String hiAndLow;

                //get first object from array
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //date return as a long, convert to readable date
                long dateTime = dayForecast.getLong(OWN_DATETIME);
                day = getReadableDateString(dateTime);

                //description is in a child array called "weather", which is 1 element long
                description = dayForecast.getJSONArray(OWN_WEATHER).getJSONObject(0).getString(OWN_DESCRIPTION);

                //temperatures are in child object called temp.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWN_TEMPERATURE);
                double hi = temperatureObject.getDouble(OWN_MAX);
                double low = temperatureObject.getDouble(OWN_MIN);
                hiAndLow = formatHiAndLow(hi, low);

                resultStrs[i] = day + " - " + description + " - " + hiAndLow;
            }
            //log the resultStrs
            for(String s : resultStrs)
                Log.v(TAG, "Forecast entry: " + s);

            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] result) {
            //update the list view with real data feed
            if(result != null){
                mForecastAdapter.clear();
                for (String s : result)
                    mForecastAdapter.add(s);
            }

        }

        // Prepare the weather high/lows for presentation.
        private String formatHiAndLow(double hi, double low) {
            long roundedHigh = Math.round(hi);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String getReadableDateString(long dateTime) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(dateTime * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

    }
}