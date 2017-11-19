package com.example.jan.locationservices;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    TextView latitudeValue;
    TextView longitudeValue;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double mylongitude;
    private double mylatitude;
    private String mytime;
    private Geocoder mylocation;
    private SimpleAdapter addressAdapter;
    public List<Address> myAddress;
    private ListView addressList;
    private Location myLocation;
    private ArrayList<Map<String, String>> addresses;
    String[] from = {"name", "time", "longitude", "latitude", "address", "postal"};
    int[] to = {R.id.addrName, R.id.addrTime, R.id.addrLong, R.id.addrLat, R.id.address, R.id.postal};
    DBHelper mydb;
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

    /**
    Pulls address object from location
     */
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

    /**
    Uses Location.distanceBetween() to calculate distances
    between 2 locations
     */
    private double distanceChecker(Map coord1) {
        double lat1 = Double.parseDouble(String.valueOf(coord1.get("latitude")));
        double lat2 = mylatitude;
        double lng1 = Double.parseDouble(String.valueOf(coord1.get("longitude")));
        double lng2 = mylongitude;

        Log.d("values", lat1 + ", " + lng1 + ": " + lat2 + ", " + lng2);
        float results[] = new float[1];
        LatLng oldPosition = new LatLng(lat1,lng1);
        LatLng newPosition = new LatLng(lat2, lng2);
        try {
            Location.distanceBetween(oldPosition.latitude, oldPosition.longitude,
                    newPosition.latitude, newPosition.longitude, results);
        } catch (IllegalArgumentException e) {
            Log.d("error", "Error is " + e);
        }


        return (double) results[0];
    }

    /**
    compares current location with store
    locations and determines the closest one
    returns id of closest location
     */
    private int findShortest (ArrayList<Map<String, String>> locations) {
        int size = locations.size();
        double shortest = Double.MAX_VALUE;
        int shortestId = -1;
        int tempId = shortestId;
        double temp;

        if(locations.isEmpty()){
            return -1;
        } else {
            while(size > 0) {
                Map<String, String> tempLocation = locations.get(size - 1);
                temp = distanceChecker(tempLocation);
                Log.d("asd", "the distance value is " + temp);
                if (temp < shortest) {
                    shortest = temp;
                    tempId = Integer.parseInt(tempLocation.get("id"));

                }
                size--;
            }
            if (shortest < 30) {
                shortestId = tempId;
            }
        }
        return shortestId;
    }

    /**
    button function for checking in the
    current location
     */
    public void onCheckIn(View v) {
        EditText et = (EditText) findViewById(R.id.etNewAddress);
        String name = et.getText().toString();
        Address tempAddress = getAddress();

        String address, postal;
        if(tempAddress == null) {
            address = "n/a";
            postal = "n/a";
        } else {
            address = String.valueOf(tempAddress.getLocality());
            postal = String.valueOf(tempAddress.getAddressLine(0));
        }

        locations = mydb.getAllLocations();
        String size = String.valueOf(locations.size());

        int idDistance = findShortest(locations);
        mytime = Calendar.getInstance().getTime().toString();
        //Toast.makeText(this, "id is " + idDistance, Toast.LENGTH_LONG).show();

        if(idDistance > 0) {
            mydb.insertCD(String.valueOf(idDistance),String.valueOf(mylongitude),String.valueOf(mylatitude),String.valueOf(mytime));
        } else {
            mydb.insertLocation(name, String.valueOf(mylatitude), String.valueOf(mylongitude), address, postal);
            ArrayList<Map<String, String>> locations = mydb.getAllLocations();
            String newSize = String.valueOf(locations.size());
            mydb.insertCD(newSize, String.valueOf(mylongitude), String.valueOf(mylatitude), String.valueOf(mytime));
        }

        addresses = mydb.getAllCoords();
        addressAdapter = new SimpleAdapter(this, addresses, R.layout.list_viewadater, from, to);
        addressList.setAdapter(addressAdapter);

        et.setText("");

    }

    /**
    button function to switch to map view
     */
    public void onSwitch(View v) {
        addresses = mydb.getAllCoords();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("location", addresses);
        startActivity(intent);
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
        myLocation = location;
        mylatitude = location.getLatitude();
        mylongitude = location.getLongitude();
        latitudeValue.setText(String.valueOf(mylatitude));
        longitudeValue.setText(String.valueOf(mylongitude));
    }
}
