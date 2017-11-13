package com.example.jan.locationservices;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jan on 11/12/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String COORDINATES_TABLE_NAME = "coordinates";
    public static final String COORDINATES_COLUMN_ID = "id";
    public static final String COORDINATES_COLUMN_NAME = "name";
    public static final String COORDINATES_COLUMN_LONGITUDE = "longitude";
    public static final String COORDINATES_COLUMN_LATITUDE = "latitude";
    public static final String COORDINATES_COLUMN_TIME = "time";
    public static final String COORDINATES_COLUMN_ADDRESS = "address";

    public DBHelper(Context context) { super(context,DATABASE_NAME,null,1);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table coordinate " + "(id integer primary key, name text, longitude double, latitude double, time long, address string)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion1) {
        db.execSQL("DROP TABLE IF EXISTS coordinate");
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
}
