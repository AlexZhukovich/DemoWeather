package com.alexzh.demoweather;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;

public class DaysListFragment extends Fragment {

    private StaggeredGridLayoutManager mGridLayoutManager;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;
    private double mLatitude;
    private double mLongitude;
    private String mUnits;

    public DaysListFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (Utility.isInternetConnection(getActivity()))
                new FetchWeatherTask().execute(String.valueOf(mLatitude), String.valueOf(mLongitude));
            else
                Toast.makeText(getActivity(), R.string.connection_problem, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUnits == null || !mUnits.equals(Utility.getUnits(getActivity()))) {
            mUnits = Utility.getUnits(getActivity());
            if (Utility.isInternetConnection(getActivity()))
                new FetchWeatherTask().execute(String.valueOf(mLatitude), String.valueOf(mLongitude));
            else
                Toast.makeText(getActivity(), R.string.connection_problem, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_days_list, container, false);

        List<String> list = new ArrayList<String>();

        mGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new WeatherAdapter(getActivity(), list);
        mRecyclerView.setAdapter(mAdapter);

        if (getActivity().getIntent().getExtras() != null) {
            mLatitude = getActivity().getIntent().getDoubleExtra(DaysListActivity.LATITUDE_KEY, 0.0);
            mLongitude = getActivity().getIntent().getDoubleExtra(DaysListActivity.LONGITUDE_KEY, 0.0);
        }

        if (Utility.isInternetConnection(getActivity()))
            new FetchWeatherTask().execute(String.valueOf(mLatitude), String.valueOf(mLongitude));
        else
            Toast.makeText(getActivity(), R.string.connection_problem, Toast.LENGTH_SHORT).show();
        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time){
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM d");
            return format.format(date).toString();
        }

        private String formatTemperature(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {

            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] results = new String[weatherArray.length()];
            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_DESCRIPTION);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatTemperature(high, low);
                results[i] = day + " - " + description + " ( " + highAndLow + " )";
            }

            for (String s : results) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return results;

        }
        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try {
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast?";
                final String UNITS_PARAM = "units";
                final String LATITUDE_PARAM = "lat";
                final String LONGITUDE_PARAM = "lon";


                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LATITUDE_PARAM, params[0])
                        .appendQueryParameter(LONGITUDE_PARAM, params[1])
                        .appendQueryParameter(UNITS_PARAM, mUnits)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
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
                return getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mAdapter.clear();
                for(String dayForecastStr : result) {
                    mAdapter.add(dayForecastStr);
                }
            }
        }
    }
}