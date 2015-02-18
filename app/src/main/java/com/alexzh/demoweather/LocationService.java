package com.alexzh.demoweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class LocationService implements LocationListener {
    private final Context mContext;

    private boolean mGPSEnabled;
    private boolean mNetworkEnabled;

    private Location mLocation;
    private LocationManager mLocationManager;
    private double mLatitude;
    private double mLongitude;

    public LocationService(Context mContext) {
        this.mContext = mContext;
        updateLocation();
    }

    public boolean isGPSEnabled() {
        return mGPSEnabled;
    }

    public boolean isNetworkEnabled() {
        return mNetworkEnabled;
    }

    public void  updateLocation() {
        mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        mGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (mGPSEnabled || mNetworkEnabled) {
            mGPSEnabled = true;
            if (mNetworkEnabled) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0,
                        this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLocation != null) {
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                    }
                }
                if (mGPSEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0,
                                0,
                                this);
                        if (mLocationManager != null) {
                            mLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                mLatitude = mLocation.getLatitude();
                                mLongitude = mLocation.getLongitude();
                            }
                        }
                    }
                }
            }
        }
    }

    public double getLatitude() {
        if (mLatitude == 0.0d) {
            updateLocation();
        }
        return mLatitude;
    }

    public double getLongitude() {
        if (mLongitude == 0.0d) {
            updateLocation();
        }
        return mLongitude;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setMessage(R.string.text_gps_settings);
        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
