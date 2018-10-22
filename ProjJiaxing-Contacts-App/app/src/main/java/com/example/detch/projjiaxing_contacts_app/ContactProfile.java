package com.example.detch.projjiaxing_contacts_app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactProfile extends AppCompatActivity {
    TextView viewName;
    TextView viewPhone;
    ListView viewRelationships;
    ImageButton profilePhoto;
    ArrayList<Map<String,String>> liteContactBook;
    String name;
    String phone;
    String relationshipsString;
    String[] relationshipNames;
    Bitmap viewPhoto;
    PhotoRepo repo; // database helper
    boolean hasPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        viewName=(TextView)findViewById(R.id.viewNameBox);
        viewPhone=(TextView)findViewById(R.id.viewPhoneBox);
        viewRelationships=(ListView)findViewById(R.id.viewRelationshipList);
        profilePhoto=(ImageButton)findViewById(R.id.viewOrAddPhoto);
        name=this.getIntent().getStringExtra("name");
        phone=this.getIntent().getStringExtra("phone");
        relationshipsString=this.getIntent().getStringExtra("relationships");

        viewName.setText(name);
        viewPhone.setText(phone);
        if(relationshipsString.length()>0){
            //relationshipNames=relationshipsString.split(";")[0].split("[,]");
            StringBuffer tmp=new StringBuffer(relationshipsString.split(";")[0]);
            tmp.deleteCharAt(tmp.length()-1);
            tmp.deleteCharAt(0);
            relationshipNames=tmp.toString().split(",");
        }
        else{ }

        liteContactBook=new ArrayList<Map<String, String>>();
        for(int i=0;i<relationshipNames.length;i++){
            Map<String,String> person=new HashMap<String ,String>();
            person.put("name",relationshipNames[i]);
            liteContactBook.add(person);
        }
        SimpleAdapter viewRelationshipAdapter = new SimpleAdapter(
                ContactProfile.this,liteContactBook,R.layout.relationship_without_checkbox_per_item,new String[]{"name"},new int[]{R.id.watchRelationshipName});
        viewRelationships.setAdapter(viewRelationshipAdapter);
        viewRelationshipAdapter.notifyDataSetChanged();
        // Now set database
        repo = new PhotoRepo(this);
        // Now set profile photo
        ArrayList<String> namesWithPhoto = repo.getNames();
        hasPhoto = false;
        for (String nameWithPhoto : namesWithPhoto) {
            if (this.name.equals(nameWithPhoto)) {
                hasPhoto = true;
            }
        }
        if (hasPhoto) {
            Person viewedPerson = repo.getPersonByName(this.name);
            this.viewPhoto = viewedPerson.getPhoto();
            Log.e("SQLite","Found photo for " + this.name + ", loading photo.");
        }
        else {
            this.viewPhoto = BitmapFactory.decodeResource(getResources(),R.drawable.nullphoto);
            Log.e("SQLite","No photo for " + this.name + ", loading null-photo.");
        }
        profilePhoto.setImageBitmap(viewPhoto);

        // Set listeners
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPhoto) {
                    // Start camera for a photo
                    Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent doneViewing=new Intent();
        //doneViewing.putExtra("photo",this.encodedPhoto);
        //Log.e("Photo Trace","photo send back to main");
        doneViewing.putExtra("name",this.name);
        ContactProfile.this.setResult(201,doneViewing);
        ContactProfile.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Back from camera
        if(resultCode == RESULT_OK){
            // Fetch photo and resize
            Bitmap rawPhoto = (Bitmap) data.getExtras().get("data");
            int w = rawPhoto.getWidth();
            int h = rawPhoto.getHeight();
            int squareWidth = w>=h?h:w;// Width after croping
            Bitmap croppedPhoto = Bitmap.createBitmap(rawPhoto,(w-squareWidth)/2,(h-squareWidth)/2,squareWidth,squareWidth);
            Bitmap photo = Bitmap.createScaledBitmap(croppedPhoto,200,200,true);
            rawPhoto = Bitmap.createScaledBitmap(croppedPhoto,1080,1080,true);
            // Now show the photo
            profilePhoto.setImageBitmap(photo);
            this.viewPhoto = photo;
            //this.encodedPhoto = BitmapToBytes(photo);
            Log.e("Photo","Photo taken and resized.");
            Person newFriend = new Person();
            newFriend.name = this.name;
            newFriend.setPhoto(this.viewPhoto);
            newFriend.setRawPhoto(rawPhoto);
            repo.insert(newFriend);
            Log.e("SQLite","Added photo for " + this.name);
        } else {
            Toast.makeText(this.getApplicationContext(),
                    "Profile photo not added",Toast.LENGTH_SHORT).show();
            Log.e("Photo", "Photo taking failed.");
        }
    }

    public byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Bitmap BytesToBitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public String BytesToBase64(byte[] bt) {
        return Base64.encodeToString(bt,Base64.DEFAULT);
    }

    public byte[] Base64ToBytes(String st) {
        return Base64.decode(st,Base64.NO_WRAP);
    }


    public class DBHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION=3;
        private static final String DATABASE_NAME="ContactAlbum.db";

        public DBHelper(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE_ALBUM="CREATE TABLE "+ Person.TABLE+"("
                    +Person.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    +Person.KEY_name+" TEXT, "
                    +Person.KEY_rawPhoto+" TEXT, "
                    +Person.KEY_photo+" TEXT)";
            db.execSQL(CREATE_TABLE_ALBUM);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ Person.TABLE);
            onCreate(db);
        }
    }

    public class Person {
        public static final String TABLE="album";

        public static final String KEY_ID="id";
        public static final String KEY_name="name";
        public static final String KEY_photo="photo";
        public static final String KEY_rawPhoto="rawphoto";

        public int ID;
        public String name;
        public String photo;
        public String rawPhoto;

        public Person () {
            this.ID = (int)System.currentTimeMillis();
        }

        public void setPhoto (Bitmap mappedPhoto) {
            this.photo = BytesToBase64(BitmapToBytes(mappedPhoto));
        }

        public void setRawPhoto (Bitmap mappedPhoto) {
            this.rawPhoto = BytesToBase64(BitmapToBytes(mappedPhoto));
        }

        public Bitmap getPhoto () {
            return BytesToBitmap(Base64ToBytes(this.photo));
        }

        public Bitmap getRawPhoto () {
            return BytesToBitmap(Base64ToBytes(this.rawPhoto));
        }

    }

    public class PhotoRepo {
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
    }
}