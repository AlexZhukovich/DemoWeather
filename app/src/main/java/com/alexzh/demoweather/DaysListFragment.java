package com.alexzh.demoweather;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alexzh.demoweather.data.WeatherContract.*;
import com.alexzh.demoweather.sync.WeatherSyncAdapter;

public class DaysListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int WEATHER_LOADER = 0;

    private StaggeredGridLayoutManager mGridLayoutManager;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;
    private double mLatitude;
    private double mLongitude;

    private static final String[] DAYS_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATE
    };

    public static int COL_WEATHER_DATE = 1;

    private void updateWeather() {
        WeatherSyncAdapter.syncImmediately(
                getActivity(),
                String.valueOf(mLatitude),
                String.valueOf(mLongitude));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_days_list, container, false);

        mGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new WeatherAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);

        if (getActivity().getIntent().getExtras() != null) {
            mLatitude = getActivity().getIntent().getDoubleExtra(DaysListActivity.LATITUDE_KEY, 0.0);
            mLongitude = getActivity().getIntent().getDoubleExtra(DaysListActivity.LONGITUDE_KEY, 0.0);
        }
        updateWeather();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";

        Uri daysUri = WeatherEntry.buildDaysUri();

        return new CursorLoader(
                getActivity(),
                daysUri,
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