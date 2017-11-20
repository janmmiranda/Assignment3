package com.example.jan.locationservices;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.jan.locationservices.R.id.map;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Button mGoBack;
    private Location mylocation;
    private ArrayList<Map<String, String>> addressList;
    private static final String KEY_LOCATION = "location";
    LatLng current;
    DBHelper mydb;
    /*
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("string", "onCreate is here");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mylocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setContentView(R.layout.activity_map);
        mydb = MainActivity.mydb;
        addressList = mydb.getAllLocations();

        mylocation = getIntent().getExtras().getParcelable("location");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("string", "onMapReady: this runs");
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

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
        mMap.setMyLocationEnabled(true);

        // Keep the UI Settings state in sync with the checkboxes.
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

        // Add a marker in Sydney and move the camera
        if(mylocation != null) {
            double lat = mylocation.getLatitude();
            double lng = mylocation.getLongitude();
            current = new LatLng(lat,lng);

            mMap.addMarker(new MarkerOptions().position(current));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        }
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                return false;
            }
        });

        addMarkers();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                double latVal = point.latitude;
                double lngVal = point.longitude;
                List<android.location.Address> addresses = null;
                android.location.Address address = null;
                try {
                    addresses = geocoder.getFromLocation(latVal, lngVal, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null && !addresses.isEmpty())
                     address = addresses.get(0);

                String addressL, postal;
                if(address == null) {
                    addressL = "n/a";
                    postal = "n/a";
                } else {
                    addressL = String.valueOf(address.getLocality());
                    postal = String.valueOf(address.getAddressLine(0));
                }

                mMap.addMarker(new MarkerOptions().position(point));
                EditText et = (EditText) findViewById(R.id.etMapAddress);
                String word = et.getText().toString();
                mydb.insertLocation(word,String.valueOf(point.latitude),String.valueOf(point.longitude), addressL, postal);
                et.setText("");
            }
        });
    }

    private void addMarkers() {
        Log.d("String", "addMarkers runs");
        int size = addressList.size();
        Log.d("string", "addreslist size is " + size);
        while (size > 0) {
            Map<String, String> tempLocation = addressList.get(size - 1);
            LatLng tempCoordinates = new LatLng(Double.parseDouble(String.valueOf(tempLocation.get("latitude"))),
                    Double.parseDouble(String.valueOf(tempLocation.get("longitude"))));
            mMap.addMarker(new MarkerOptions().position(tempCoordinates));
            size--;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_LOCATION, mylocation);
            super.onSaveInstanceState(outState);
        }
    }
}
