package com.example.jan.locationservices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jan on 11/12/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";

    public static final String RELATIONS_TABLE_NAME = "relations";
    public static final String RELATIONS_COLUMN_ASSOCIATES = "associates";

    public static final String COORDINATES_TABLE_NAME = "coordinates";
    public static final String COORDINATES_COLUMN_ID = "id";
    public static final String COORDINATES_COLUMN_NAME = "name";
    public static final String COORDINATES_COLUMN_LONGITUDE = "longitude";
    public static final String COORDINATES_COLUMN_LATITUDE = "latitude";
    public static final String COORDINATES_COLUMN_TIME = "time";
    public static final String COORDINATES_COLUMN_ADDRESS = "address";




    public DBHelper(Context context) {
        super(context,DATABASE_NAME,null,2);
        context.deleteDatabase(DATABASE_NAME);
    }

    private static final String CREATE_TABLE_RELATIONS = "CREATE TABLE " + RELATIONS_TABLE_NAME +
            " (" + COORDINATES_COLUMN_ID + " INTEGER PRIMARY KEY, " + RELATIONS_COLUMN_ASSOCIATES
            + " TEXT)";

    private static final String CREATE_TABLE_COORDINATES = "CREATE TABLE " + COORDINATES_TABLE_NAME +
            " (" + COORDINATES_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            COORDINATES_COLUMN_NAME + " TEXT, " + COORDINATES_COLUMN_LONGITUDE + " TEXT, " + COORDINATES_COLUMN_LATITUDE +
            " TEXT, " + COORDINATES_COLUMN_TIME + " TEXT, " + COORDINATES_COLUMN_ADDRESS + " TEXT)";



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COORDINATES);
        db.execSQL(CREATE_TABLE_RELATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion1) {
        db.execSQL("DROP TABLE IF EXISTS" +  COORDINATES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS" +  RELATIONS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertCD(String name, String longitude, String latitude, String time, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("longitude", longitude);
        contentValues.put("latitude", latitude);
        contentValues.put("time", time);
        contentValues.put("address", address);
        db.insert("coordinates", null, contentValues);
        return true;
    }

    public boolean insertRelation(String relate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("associates", relate);
        return true;
    }

    public HashMap<String, String> getCoord(int i) {
        HashMap<String, String> coordinate = new HashMap<String, String>(2);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from coordinates where id = '" + i + "'", null);
        res.moveToFirst();
        String name, longitude, latitude, time, address, id;
        if(res.moveToFirst()) {
            do {
                name = res.getString(res.getColumnIndex(COORDINATES_COLUMN_NAME));
                longitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LONGITUDE));
                latitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LATITUDE));
                time = res.getString(res.getColumnIndex(COORDINATES_COLUMN_TIME));
                address = res.getString(res.getColumnIndex(COORDINATES_COLUMN_ADDRESS));
                coordinate.put("name", name);
                coordinate.put("longitude", longitude);
                coordinate.put("latitude", latitude);
                coordinate.put("time", time);
                coordinate.put("address", address);
            } while(res.moveToNext());
        }
        return coordinate;
    }

    public ArrayList<Map<String, String>> getAllCoords() {
        ArrayList<Map<String, String>> array_List = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from coordinates", null);
        res.moveToFirst();
        String name, longitude, latitude, time, address, id;
        while(res.isAfterLast() == false) {
            HashMap<String, String> coordinates = new HashMap<String, String>(2);
            name = res.getString(res.getColumnIndex(COORDINATES_COLUMN_NAME));
            longitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LONGITUDE));
            latitude = res.getString(res.getColumnIndex(COORDINATES_COLUMN_LATITUDE));
            time = res.getString(res.getColumnIndex(COORDINATES_COLUMN_TIME));
            address = res.getString(res.getColumnIndex(COORDINATES_COLUMN_ADDRESS));
            coordinates.put("name", name);
            coordinates.put("longitude", longitude);
            coordinates.put("latitude", latitude);
            coordinates.put("time", time);
            coordinates.put("address", address);
            array_List.add(coordinates);
            res.moveToNext();
        }
        return array_List;
    }

}
