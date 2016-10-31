package com.tonydantona.navigator;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

// Created by rti1ajd on 3/22/2016.

public class LocationServices implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private static LocationServices instance = null;

    private ILocationServices mLocationListener;


    private static Location mLocation;

    // Singleton implementation
    public static LocationServices getLocationManager(Context context)     {
        if (instance == null) {
            instance = new LocationServices(context);
        }
        return instance;
    }

    // Local constructor
    private LocationServices( Context context )     {
        mLocationListener = (ILocationServices) context;
        initLocationService(context);
    }

    public static Location getLocation() {
        return mLocation;
    }

     // Sets up location service after permissions is granted
    @TargetApi(23)
    private void initLocationService(Context context) {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if ( !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)  ) {
                return;
            }

            if ( !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                mLocationListener.onLocationServicesProviderDisabled("GPS Disabled");
            }
            else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (mLocation==null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

        } catch (Exception ex)  {
            ex.printStackTrace();
            Log.e("LocationServices", ex.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location)     {
        if (location == null)
            return;
        mLocationListener.onLocationServicesLocationChange(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        mLocationListener.onLocationServicesStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        mLocationListener.onLocationServicesProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        mLocationListener.onLocationServicesProviderDisabled(provider);
    }

    public interface ILocationServices {
        void onLocationServicesLocationChange(Location location);
        void onLocationServicesStatusChanged(String provider, int status, Bundle extras);
        void onLocationServicesProviderEnabled(String provider);
        void onLocationServicesProviderDisabled(String provider);
    }

}

