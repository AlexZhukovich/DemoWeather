package com.alexzh.demoweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LocationFragment extends Fragment {
    private Button mLocationButton;
    private Button mSearchButton;
    private EditText mLatitudeEditText, mLongitudeEditText;

    public LocationFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mLocationButton = (Button) rootView.findViewById(R.id.location_button);
        mSearchButton = (Button) rootView.findViewById(R.id.search_button);
        mLatitudeEditText = (EditText) rootView.findViewById(R.id.latitude_edittext);
        mLongitudeEditText = (EditText) rootView.findViewById(R.id.longitude_edittext);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationService locationService = new LocationService(getActivity());
                if (locationService.isGPSEnabled() || locationService.isNetworkEnabled()) {
                    mLatitudeEditText.setText(String.valueOf(locationService.getLatitude()));
                    mLongitudeEditText.setText(String.valueOf(locationService.getLongitude()));
                } else {
                    locationService.showSettingsAlert();
                }

            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent = new Intent(getActivity(), DaysListActivity.class);
                try {
                    double latitude = Double.valueOf(mLatitudeEditText.getText().toString());
                    double longitude = Double.valueOf(mLongitudeEditText.getText().toString());
                    listIntent.putExtra(DaysListActivity.LATITUDE_KEY, latitude);
                    listIntent.putExtra(DaysListActivity.LONGITUDE_KEY, longitude);
                    getActivity().startActivity(listIntent);
                }catch (NumberFormatException ex) {
                    Toast.makeText(getActivity(), R.string.enter_correct_date, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }
}