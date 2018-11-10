package com.example.detch.projjintang_daily_path;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




public class GeoRepo {
    private DBHelper dbHelper;

    public PhotoRepo(Context context){
        dbHelper=new DBHelper(context);
    }

    public int insert(Person person){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Person.KEY_ID, person.ID);
        values.put(Person.KEY_name, person.name);
        values.put(Person.KEY_rawPhoto, person.rawPhoto);
        values.put(Person.KEY_photo, person.photo);
        long person_Id=db.insert(Person.TABLE,null,values);
        db.close();
        return (int)person_Id;
    }

    public void delete(int Id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(Person.TABLE,Person.KEY_ID+"=?", new String[]{String.valueOf(Id)});
        db.close();
    }
    public void update (Person person) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Person.KEY_name, person.name);
        values.put(Person.KEY_photo, person.photo);
        values.put(Person.KEY_rawPhoto, person.rawPhoto);
        db.update(Person.TABLE,values,Person.KEY_ID+"=?",new String[] { String.valueOf(person.ID) });
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAll () {
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Person.KEY_ID+","+
                Person.KEY_name+","+
                Person.KEY_rawPhoto+","+
                Person.KEY_photo+" FROM "+Person.TABLE;
        ArrayList<HashMap<String,String>> list=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> person=new HashMap<String,String>();
                //person.put("id",cursor.getString(cursor.getColumnIndex(Person.KEY_ID)));
                person.put("name",cursor.getString(cursor.getColumnIndex(Person.KEY_name)));
                person.put("photo",cursor.getString(cursor.getColumnIndex(Person.KEY_photo)));
                person.put("rawPhoto",cursor.getString(cursor.getColumnIndex(Person.KEY_rawPhoto)));
                list.add(person);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Person getPersonById(int Id){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Person.KEY_ID + "," +
                Person.KEY_name + "," +
                Person.KEY_rawPhoto + "," +
                Person.KEY_photo +
                " FROM " + Person.TABLE
                + " WHERE " +
                Person.KEY_ID + "=?";
        Person person = new Person();
        Cursor cursor=db.rawQuery(selectQuery,new String[]{String.valueOf(Id)});
        if(cursor.moveToFirst()){
            do{
                person.ID = cursor.getInt(cursor.getColumnIndex(Person.KEY_ID));
                person.name = cursor.getString(cursor.getColumnIndex(Person.KEY_name));
                person.photo  = cursor.getString(cursor.getColumnIndex(Person.KEY_photo));
                person.rawPhoto  = cursor.getString(cursor.getColumnIndex(Person.KEY_rawPhoto));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return person;
    }

    public Person getPersonByName(String name){
        Log.e("SQLite", "Get Person by name: " + name);
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Person.KEY_ID + "," +
                Person.KEY_name + "," +
                Person.KEY_rawPhoto + "," +
                Person.KEY_photo +
                " FROM " + Person.TABLE
                + " WHERE " +
                Person.KEY_name + "=?";
        Person person = new Person();
        Cursor cursor=db.rawQuery(selectQuery, new String[]{name});
        if(cursor.moveToFirst()){
            do{
                person.ID = cursor.getInt(cursor.getColumnIndex(Person.KEY_ID));
                person.name = cursor.getString(cursor.getColumnIndex(Person.KEY_name));
                person.photo  = cursor.getString(cursor.getColumnIndex(Person.KEY_photo));
                person.rawPhoto  = cursor.getString(cursor.getColumnIndex(Person.KEY_rawPhoto));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return person;
    }

    public ArrayList<String> getNames () {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Person.KEY_ID+","+
                Person.KEY_name+","+
                Person.KEY_photo+" FROM "+Person.TABLE;
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                list.add(cursor.getString(cursor.getColumnIndex(Person.KEY_name)));
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

    public class Geo {
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
    }
}

