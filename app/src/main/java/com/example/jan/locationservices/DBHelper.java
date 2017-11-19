package com.example.jan.locationservices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;
import static android.R.attr.name;
import static com.example.jan.locationservices.R.id.add;
import static com.example.jan.locationservices.R.id.address;

/**
 * Created by Jan on 11/12/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";

    public static final String LOCATION_TABLE_NAME = "location";
    public static final String LOCATION_COLUMN_ID = "id";
    public static final String LOCATION_COLUMN_NAME = "name";
    public static final String LOCATION_COLUMN_LONGITUDE = "longitude";
    public static final String LOCATION_COLUMN_LATITUDE = "latitude";
    public static final String LOCATION_COLUMN_ADDRESS = "address";
    public static final String LOCATION_COLUMN_POSTAL = "postal";

    public static final String COORDINATES_TABLE_NAME = "coordinates";
    public static final String COORDINATES_COLUMN_ID = "id";
    public static final String COORDINATES_COLUMN_IDLOCATION = "idLocation";
    public static final String COORDINATES_COLUMN_LONGITUDE = "longitude";
    public static final String COORDINATES_COLUMN_LATITUDE = "latitude";
    public static final String COORDINATES_COLUMN_TIME = "time";

    public DBHelper(Context context) {
        super(context,DATABASE_NAME,null,2);
        context.deleteDatabase(DATABASE_NAME);
    }

    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE " + LOCATION_TABLE_NAME +
            " (" + COORDINATES_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            LOCATION_COLUMN_NAME + " TEXT," +
            LOCATION_COLUMN_LATITUDE + " TEXT," +
            LOCATION_COLUMN_LONGITUDE + " TEXT," +
            LOCATION_COLUMN_ADDRESS + " TEXT," +
            LOCATION_COLUMN_POSTAL + " TEXT)";

    private static final String CREATE_TABLE_COORDINATES = "CREATE TABLE " + COORDINATES_TABLE_NAME +
            " (" + COORDINATES_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            COORDINATES_COLUMN_IDLOCATION + " TEXT, " +
            COORDINATES_COLUMN_LONGITUDE + " TEXT, " +
            COORDINATES_COLUMN_LATITUDE + " TEXT, " +
            COORDINATES_COLUMN_TIME + " TEXT)";



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COORDINATES);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion1) {
        db.execSQL("DROP TABLE IF EXISTS" +  COORDINATES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS" +  LOCATION_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertCD(String idlocation, String longitude, String latitude, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("idLocation", idlocation);
        contentValues.put("longitude", longitude);
        contentValues.put("latitude", latitude);
        contentValues.put("time", time);
        db.insert("coordinates", null, contentValues);
        return true;
    }

    public boolean insertLocation(String name, String latitude, String longitude, String address, String postal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("address", address);
        contentValues.put("postal", postal);
        db.insert("location", null, contentValues);
        return true;
    }

    public HashMap<String, String> getLocation(int i) {
        HashMap<String, String> location = new HashMap<String, String>(2);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from location where idLocation = '" + i + "'", null);
        String name, longitude, latitude, address, postal;
        if(res.moveToFirst()) {
            do {
                name = res.getString(res.getColumnIndex(LOCATION_COLUMN_NAME));
                longitude = res.getString(res.getColumnIndex(LOCATION_COLUMN_LONGITUDE));
                latitude = res.getString(res.getColumnIndex(LOCATION_COLUMN_LATITUDE));
                address = res.getString(res.getColumnIndex(LOCATION_COLUMN_ADDRESS));
                postal = res.getString(res.getColumnIndex(LOCATION_COLUMN_POSTAL));
                location.put("name", name);
                location.put("longitude", longitude);
                location.put("latitude", latitude);
                location.put("address", address);
                location.put("postal", postal);
            } while(res.moveToNext());
        }
        return location;
    }

    public ArrayList<Map<String, String>> getAllLocations() {
        ArrayList<Map<String, String>> array_List = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from location", null);
        res.moveToFirst();
        String id, name, longitude, latitude, address, postal;
        while(res.isAfterLast() == false) {
            HashMap<String, String> location = new HashMap<String, String>(2);
            id = res.getString(res.getColumnIndex(LOCATION_COLUMN_ID));
            name = res.getString(res.getColumnIndex(LOCATION_COLUMN_NAME));
            longitude = res.getString(res.getColumnIndex(LOCATION_COLUMN_LONGITUDE));
            latitude = res.getString(res.getColumnIndex(LOCATION_COLUMN_LATITUDE));
            address = res.getString(res.getColumnIndex(LOCATION_COLUMN_ADDRESS));
            postal = res.getString(res.getColumnIndex(LOCATION_COLUMN_POSTAL));
            location.put("id", id);
            location.put("name", name);
            location.put("longitude", longitude);
            location.put("latitude", latitude);
            location.put("address", address);
            location.put("postal", postal);
            array_List.add(location);
            res.moveToNext();
        }
        return array_List;
    }

    // SELECT * FROM coordinates c JOIN location l ON l.id = c.location_id

    public ArrayList<Map<String, String>> getAllCoords() {
        ArrayList<Map<String, String>> array_List = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from coordinates c join location l on l.id = c.idLocation", null);
        res.moveToFirst();
        String name, longitude, latitude, time, address, postal;
        while(res.isAfterLast() == false) {
            HashMap<String, String> coordinates = new HashMap<String, String>(2);
            name = res.getString(res.getColumnIndex(LOCATION_COLUMN_NAME));
            longitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LONGITUDE));
            latitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LATITUDE));
            time = res.getString(res.getColumnIndex(COORDINATES_COLUMN_TIME));
            address = res.getString(res.getColumnIndex(LOCATION_COLUMN_ADDRESS));
            postal = res.getString(res.getColumnIndex(LOCATION_COLUMN_POSTAL));
            coordinates.put("name", name);
            coordinates.put("longitude", longitude);
            coordinates.put("latitude", latitude);
            coordinates.put("time", time);
            coordinates.put("address", address);
            coordinates.put("postal", postal);
            array_List.add(coordinates);
            res.moveToNext();
        }
        return array_List;
    }

}
