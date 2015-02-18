package com.alexzh.demoweather;

import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexzh.demoweather.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final static int WEATHER_TIME_LOADER = 0;
    private final static int NUM_GRID_COLUMN = 1;

    public static final String CURRENT_DATE = "current_date";

    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    private Cursor mNewDataCursor;
    private String mLatitude;
    private String mLongitude;
    private long mFirstDate;
    private Uri mWeatherUri;
    private boolean isMetric;

    private static final String[] DAYS_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_LOCATION_ID,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_DESCRIPTION,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_WIND
    };

    public static final int COL_DATE = 2;
    public static final int COL_MAX_TEMP = 3;
    public static final int COL_MIN_TEMP = 4;
    public static final int COL_DESCRIPTION = 5;
    public static final int COL_HUMIDITY = 6;
    public static final int COL_PRESSURE = 7;
    public static final int COL_WIND = 8;

private StaggeredGridLayoutManager mGridLayoutManager;
    private RecyclerView mRecView;
    private WeatherTimeAdapter mAdapter;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMetric != Utility.isMetric(getActivity())) {
            isMetric = Utility.isMetric(getActivity());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mGridLayoutManager = new StaggeredGridLayoutManager(NUM_GRID_COLUMN,
                StaggeredGridLayoutManager.VERTICAL);
        mRecView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecView.setHasFixedSize(true);
        mRecView.setLayoutManager(mGridLayoutManager);
        mRecView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new WeatherTimeAdapter(getActivity(), null);
        mRecView.setAdapter(mAdapter);

        if (getActivity().getIntent().getExtras() != null) {
            mFirstDate = Long.valueOf(getActivity().getIntent().getStringExtra(CURRENT_DATE).substring(0,8));
        }

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecView.setLayoutManager(mLayoutManager);

        mRecView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if ( (visibleItemCount+pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        mFirstDate = Utility.getNextDay(mFirstDate);
                        mWeatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                mFirstDate,
                                mLatitude,
                                mLongitude);
                        mNewDataCursor = getActivity().getContentResolver().query(
                                mWeatherUri,
                                DAYS_COLUMNS,
                                null,
                                null,
                                null
                        );

                        if (mNewDataCursor.getCount() > 0) {
                            Cursor oldData = mAdapter.getCursor();
                            MergeCursor merge = new MergeCursor(new Cursor[]{oldData, mNewDataCursor});
                            mAdapter.swapCursor(merge);
                            loading = true;
                        }

                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_TIME_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri location = WeatherContract.LocationEntry.buildLocationUri();

        Cursor cursor =  getActivity().getContentResolver().query(
                location,
                null,
                null,
                null,
                null
        );

        cursor.moveToLast();

        mLatitude = cursor.getString(3);
        mLongitude = cursor.getString(4);

        mWeatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mFirstDate,
                mLatitude,
                mLongitude);

        return new CursorLoader(
                getActivity(),
                mWeatherUri,
                DAYS_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}