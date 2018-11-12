package com.example.detch.projjintang_daily_path;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GeoRepo {
    private DBHelper dbHelper;

    public GeoRepo(Context context){
        dbHelper=new DBHelper(context);
    }

    public int insert(Geo geo){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Geo.KEY_ID, geo.ID);
        values.put(Geo.KEY_name, geo.name);
        values.put(Geo.KEY_addr, geo.addr);
        values.put(Geo.KEY_lat, geo.lat);
        values.put(Geo.KEY_lon, geo.lon);
        values.put(Geo.KEY_time, geo.time);
        values.put(Geo.KEY_mode, geo.mode);
        long geo_Id=db.insert(Geo.TABLE,null,values);
        db.close();
        return (int)geo_Id;
    }

    public void delete(int Id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(Geo.TABLE,Geo.KEY_ID+"=?", new String[]{String.valueOf(Id)});
        db.close();
    }
    public void update (Geo geo) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Geo.KEY_name, geo.name);
        values.put(Geo.KEY_addr, geo.addr);
        values.put(Geo.KEY_lat, geo.lat);
        values.put(Geo.KEY_lon, geo.lon);
        values.put(Geo.KEY_time, geo.time);
        values.put(Geo.KEY_mode, geo.mode);
        db.update(Geo.TABLE,values,Geo.KEY_ID+"=?",new String[] { String.valueOf(geo.ID) });
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAll () {
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Geo.KEY_ID+","+
                Geo.KEY_name+","+
                Geo.KEY_addr+","+
                Geo.KEY_lat+","+
                Geo.KEY_lon+","+
                Geo.KEY_time+","+
                Geo.KEY_mode+" FROM "+Geo.TABLE;
        ArrayList<HashMap<String,String>> list=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> geo=new HashMap<String,String>();
                //geo.put("id",cursor.getString(cursor.getColumnIndex(Geo.KEY_ID)));
                geo.put("name",cursor.getString(cursor.getColumnIndex(Geo.KEY_name)));
                geo.put("addr",cursor.getString(cursor.getColumnIndex(Geo.KEY_addr)));
                geo.put("lat",cursor.getString(cursor.getColumnIndex(Geo.KEY_lat)));
                geo.put("lon",cursor.getString(cursor.getColumnIndex(Geo.KEY_lon)));
                geo.put("time",cursor.getString(cursor.getColumnIndex(Geo.KEY_time)));
                geo.put("mode",cursor.getString(cursor.getColumnIndex(Geo.KEY_mode)));
                list.add(geo);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Geo getGeoById(int Id){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Geo.KEY_ID + "," +
                Geo.KEY_name + "," +
                Geo.KEY_addr + "," +
                Geo.KEY_lat+","+
                Geo.KEY_lon+","+
                Geo.KEY_time+","+
                Geo.KEY_mode +
                " FROM " + Geo.TABLE
                + " WHERE " +
                Geo.KEY_ID + "=?";
        Geo geo = new Geo("","","","","","");
        Cursor cursor=db.rawQuery(selectQuery,new String[]{String.valueOf(Id)});
        if(cursor.moveToFirst()){
            do{
                geo.ID = cursor.getInt(cursor.getColumnIndex(Geo.KEY_ID));
                geo.name = cursor.getString(cursor.getColumnIndex(Geo.KEY_name));
                geo.addr  = cursor.getString(cursor.getColumnIndex(Geo.KEY_addr));
                geo.lat  = cursor.getString(cursor.getColumnIndex(Geo.KEY_lat));
                geo.lon  = cursor.getString(cursor.getColumnIndex(Geo.KEY_lon));
                geo.time  = cursor.getString(cursor.getColumnIndex(Geo.KEY_time));
                geo.mode  = cursor.getString(cursor.getColumnIndex(Geo.KEY_mode));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return geo;
    }

    public Geo getGeoByName(String name){
        Log.i("SQLite", "Get Geo by name: " + name);
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Geo.KEY_ID + "," +
                Geo.KEY_name + "," +
                Geo.KEY_addr + "," +
                Geo.KEY_lat+","+
                Geo.KEY_lon+","+
                Geo.KEY_time+","+
                Geo.KEY_mode +
                " FROM " + Geo.TABLE
                + " WHERE " +
                Geo.KEY_name + "=?";
        Geo geo = new Geo("","","","","","");
        Cursor cursor=db.rawQuery(selectQuery, new String[]{name});
        if(cursor.moveToFirst()){
            do{
                geo.ID = cursor.getInt(cursor.getColumnIndex(Geo.KEY_ID));
                geo.name = cursor.getString(cursor.getColumnIndex(Geo.KEY_name));
                geo.addr  = cursor.getString(cursor.getColumnIndex(Geo.KEY_addr));
                geo.lat  = cursor.getString(cursor.getColumnIndex(Geo.KEY_lat));
                geo.lon  = cursor.getString(cursor.getColumnIndex(Geo.KEY_lon));
                geo.time  = cursor.getString(cursor.getColumnIndex(Geo.KEY_time));
                geo.mode  = cursor.getString(cursor.getColumnIndex(Geo.KEY_mode));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return geo;
    }

    public ArrayList<String> getNames () {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Geo.KEY_ID+","+
                Geo.KEY_name+" FROM "+Geo.TABLE;
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                list.add(cursor.getString(cursor.getColumnIndex(Geo.KEY_name)));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public class DBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "geo_data_saves.db";

        public DBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE_ALBUM="CREATE TABLE "+ Geo.TABLE+"("
                    +Geo.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    +Geo.KEY_name+" TEXT, "
                    +Geo.KEY_addr+" TEXT, "
                    +Geo.KEY_lat+" TEXT, "
                    +Geo.KEY_lon+" TEXT, "
                    +Geo.KEY_time+" TEXT, "
                    +Geo.KEY_mode+" TEXT)";
            db.execSQL(CREATE_TABLE_ALBUM);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ Geo.TABLE);
            onCreate(db);
        }
    }

    public static class Geo {
        public static final String KEY_ID = "ID";
        public static final String TABLE = "geodata";
        public static final String KEY_name = "name";
        public static final String KEY_addr = "addr";
        public static final String KEY_lat = "lat";
        public static final String KEY_lon = "lon";
        public static final String KEY_time = "time";
        public static final String KEY_mode = "mode";

        public int ID;
        public String name;
        public String addr;
        public String lat;
        public String lon;
        public String time;
        public String mode;

        public Geo (String name, String addr, String lat, String lon, String time, String mode) {
            this.ID = (int)System.currentTimeMillis();
            this.name = name;
            this.addr = addr;
            this.lat = lat;
            this.lon = lon;
            this.time = time;
            this.mode = mode;
        }

        public void setId(int id) {
            this.ID = id;
        }
    }
}

