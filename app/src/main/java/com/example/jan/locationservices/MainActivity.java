package com.example.jan.locationservices;

import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    TextView latitudeValue;
    TextView longitudeValue;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double mylongitude;
    private double mylatitude;
    private Geocoder geocode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("String", "onCreate: this runs");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeValue = (TextView) findViewById(R.id.latVal);
        longitudeValue = (TextView) findViewById(R.id.lngVal);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    protected void onStart() {
        Log.d("String", "onStart: this runs");
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("String", "onResume: this runs");
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        Log.d("String", "onPause: this");
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onStop() {
        Log.d("String", "onStop: this run");
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        Log.d("String", "onConnected: this runs");
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        Log.d("String", "requestLocationUpdates: this runs");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d("String", "requestLocationUpdates: this runs");
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("String", "onConnectionSuspended: this runs");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("String", "onConnectionFailed: this runs");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("String", "onLocationChanged: this runs");
        mylatitude = location.getLatitude();
        mylongitude = location.getLongitude();
        latitudeValue.setText(String.valueOf(mylatitude));
        longitudeValue.setText(String.valueOf(mylongitude));
    }
}
