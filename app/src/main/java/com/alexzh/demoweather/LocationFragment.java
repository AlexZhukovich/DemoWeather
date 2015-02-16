package com.alexzh.demoweather;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private Button mLocationButton, mSearchButton;
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

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        mLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLocation();
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

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeEditText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeEditText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            mLatitudeEditText.setText("0.0");
            mLongitudeEditText.setText("0.0");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getActivity(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "Connection is OK");
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
}