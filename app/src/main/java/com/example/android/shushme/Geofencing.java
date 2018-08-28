package com.example.android.shushme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    private static final String TAG = Geofencing.class.getSimpleName();

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> mGeofencesList;
    private PendingIntent mPendingIntent;

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mGeofencesList = new ArrayList<>();
    }


    public void registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeofencesList == null || mGeofencesList.size() == 0) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);
        }

    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent()).setResultCallback(this);
        }

    }

    public void updateGeofencesList(PlaceBuffer places){

        if(places == null) { return; }

        mGeofencesList = new ArrayList<>();

        for(Place place: places) {
            String placeUID = place.getId();
            Double longitude = place.getLatLng().longitude;
            Double latitude = place.getLatLng().latitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setCircularRegion(latitude, longitude, 50)
                    .setExpirationDuration(1000000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofencesList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .addGeofences(mGeofencesList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mPendingIntent != null) { return mPendingIntent; }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG,"onResult");
    }
}
