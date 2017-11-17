package com.example.jan.locationservices;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    TextView latitudeValue;
    TextView longitudeValue;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double mylongitude;
    private double mylatitude;
    private long mytime;
    private Geocoder mylocation;
    private SimpleAdapter addressAdapter;
    public List<Address> myAddress;
    private ListView addressList;
    private ArrayList<Map<String, String>> addresses;
    String[] from = {"name", "time", "longitude", "latitude", "address"};
    int[] to = {R.id.addrName, R.id.addrTime, R.id.addrLong, R.id.addrLat, R.id.address};
    DBHelper mydb;
    int idlocation;
    ArrayList<Map<String, String>> locations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("String", "onCreate: this runs");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mydb = new DBHelper(this);

        locations = new ArrayList<Map<String, String>>();

        addressList = (ListView) findViewById(R.id.addressList);
        addresses = mydb.getAllCoords();
        addressAdapter = new SimpleAdapter(this, addresses, R.layout.list_viewadater, from, to);
        addressList.setAdapter(addressAdapter);

        mylocation = new Geocoder(this, Locale.getDefault());

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





    private Address getAddress() {
        try {
            myAddress = mylocation.getFromLocation(mylatitude, mylongitude, 1);
        } catch (IOException e) {
            return null;
        }
        if (myAddress != null && !myAddress.isEmpty())
            return myAddress.get(0);
        else
            return null;
    }

    private double distanceChecker(Map coord1) {
        double lat1 = Math.abs(Double.parseDouble(String.valueOf(coord1.get("latitude"))));
        double lat2 = Math.abs(mylatitude);
        double lng1 = Math.abs(Double.parseDouble(String.valueOf(coord1.get("longitude"))));
        double lng2 = Math.abs(mylongitude);

        double varR = 6371e3;
        double var1 = Math.toRadians(lat1);
        double var2 = Math.toRadians(lat2);
        double var3 = Math.toRadians(lat2 - lat1);
        double var4 = Math.toRadians(lng2 - lng1);

        double varA = Math.sin(var3/var2) * Math.sin(var3/var2) +
                Math.cos(var1) * Math.cos(var2) * Math.sin(var4/2) * Math.sin(var4/2);
        double varC = 2 * Math.atan2(Math.sqrt(varA), Math.sqrt(varA));
        double varD = varR * varC;

        return varD/1000;
    }
    private int findShortest (ArrayList<Map<String, String>> locations) {
        int size = locations.size();
        double shortest = Double.MAX_VALUE;
        int shortestId = -1;
        double temp;

        while(size > 0){
            Map<String, String> tempLocation = locations.get(size - 1);
            temp = distanceChecker(tempLocation);
            if (temp < shortest) {
                shortest = temp;
                shortestId = Integer.parseInt(tempLocation.get("id"));
            }
            size--;
        }
        Toast.makeText(this, "distance is " + shortest, Toast.LENGTH_LONG).show();
        if(shortest < 30){
            return shortestId;
        } else {
            return -1;
        }
    }

    public void onCheckIn(View v) {
        EditText et = (EditText) findViewById(R.id.etNewAddress);
        String name = et.getText().toString();
        Address tempAddress = getAddress();

        String address, postal;
        if(tempAddress.getLocality() == null) {
            address = "n/a";
        } else {
            address = String.valueOf(tempAddress.getLocality());
        }
        if(tempAddress.getAddressLine(0) == null) {
            postal = "n/a";
        } else {
            postal = String.valueOf(tempAddress.getAddressLine(0));
        }
        locations = mydb.getAllLocations();
        int idDistance = findShortest(locations);

        if(idDistance > 0) {
            mydb.insertCD(String.valueOf(idDistance),String.valueOf(mylongitude),String.valueOf(mylatitude),String.valueOf(mytime));
        } else {
            mydb.insertLocation(name, String.valueOf(mylongitude), String.valueOf(mylatitude), address, postal);
            ArrayList<Map<String, String>> locations = mydb.getAllLocations();
            String size = String.valueOf(locations.size());
            mydb.insertCD(size, String.valueOf(mylongitude), String.valueOf(mylatitude), String.valueOf(mytime));
            Toast.makeText(this, "Sent, size of " + size, Toast.LENGTH_LONG).show();
        }

        addresses = mydb.getAllCoords();
        addressAdapter = new SimpleAdapter(this, addresses, R.layout.list_viewadater, from, to);
        addressList.setAdapter(addressAdapter);


        et.setText("");

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
        mytime = location.getTime();
        latitudeValue.setText(String.valueOf(mylatitude));
        longitudeValue.setText(String.valueOf(mylongitude));
    }
}
