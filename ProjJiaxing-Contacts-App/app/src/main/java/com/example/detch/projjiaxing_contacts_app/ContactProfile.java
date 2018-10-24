package com.example.detch.projjiaxing_contacts_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactProfile extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
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
    Bitmap viewPhotoLarge;
    PhotoRepo repo; // database helper
    boolean hasPhoto;
    private Animator mCurrentAnimator;
    String mTime;
    View view;
    List<Map<String,String>> contactbook = new ArrayList<Map<String,String >>();


    public ContactProfile(){}

    public static ContactProfile newInstance(String name, String phone, String relationshipsString) {
        ContactProfile newFragment = new ContactProfile();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("phone", phone);
        bundle.putString("relationships", relationshipsString);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore last state
            mTime = savedInstanceState.getString("time_key");
        } else {
            mTime = "" + Calendar.getInstance().getTimeInMillis();
        }

        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_contact_profile);
        view = inflater.inflate(R.layout.activity_contact_profile, container, false);

        viewName=(TextView)view.findViewById(R.id.viewNameBox);
        viewPhone=(TextView)view.findViewById(R.id.viewPhoneBox);
        viewRelationships=(ListView)view.findViewById(R.id.viewRelationshipList);
        profilePhoto=(ImageButton)view.findViewById(R.id.viewOrAddPhoto);
        //name=this.getIntent().getStringExtra("name");
        //phone=this.getIntent().getStringExtra("phone");
        //relationshipsString=this.getIntent().getStringExtra("relationships");
        Bundle bundle = getArguments();
        name = bundle.getString("name");
        phone = bundle.getString("phone");
        relationshipsString = bundle.getString("relationships");

        viewName.setText(name);
        viewPhone.setText(phone);
        if(relationshipsString.length()>0){
            StringBuffer tmp=new StringBuffer(relationshipsString.split(";")[0]);
            tmp.deleteCharAt(tmp.length()-1);
            tmp.deleteCharAt(0);
            relationshipNames=tmp.toString().split(",");
        }
        else{ }
        ArrayList<String> namelist = new ArrayList<String>();
        for (Map<String,String> person : contactbook) {
            namelist.add(person.get("name"));
        }
        Log.e("namelist",namelist.toString());
        boolean exists = false;
        liteContactBook=new ArrayList<Map<String, String>>();
        for(int i=0;i<relationshipNames.length;i++){
            exists = false;
            Map<String,String> person=new HashMap<String ,String>();
            String currentName = relationshipNames[i];
            for (String existedName : namelist) {
                Log.e("checking relationships","current="+currentName+" compared to"+existedName);
                if (currentName.equals(existedName)) {
                    exists = true;
                }
            }
            if(exists) {
                person.put("name", currentName);
                liteContactBook.add(person);
            }
        }
        SimpleAdapter viewRelationshipAdapter = new SimpleAdapter(
                getActivity(),liteContactBook,R.layout.relationship_without_checkbox_per_item,new String[]{"name"},new int[]{R.id.watchRelationshipName});
        viewRelationships.setAdapter(viewRelationshipAdapter);
        viewRelationshipAdapter.notifyDataSetChanged();
        // Now set database
        repo = new PhotoRepo(getActivity());
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
                } else {
                    // Start the photo zoom animation effect
                    zoomImage(view.findViewById(R.id.viewOrAddPhoto), repo.getPersonByName(name).getRawPhoto());
                }
            }
        });

        viewRelationships.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                loadData();
                String anotherName = relationshipNames[position];
                String anotherPhone = new String();
                String anotherRelationship = new String();
                for (Map<String, String> contact : contactbook) {
                    if (contact.get("name").equals(anotherName)) {
                        anotherPhone = contact.get("phone");
                        anotherRelationship = contact.get("relationships");
                    }
                }
                if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ContactProfile fragment_3 = ContactProfile.newInstance(anotherName, anotherPhone, anotherRelationship);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container2, fragment_3);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    //return false;
                } else if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ContactProfile fragment_3 = ContactProfile.newInstance(anotherName, anotherPhone, anotherRelationship);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment_3);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

                /*
                //boolean showAnother = true;
                Intent anotherPersonInfo = new Intent(ContactProfile.this, ContactsList.class);
                anotherPersonInfo.putExtra("anotherName", anotherName);
                //personInfo.putExtra("showAnother", showAnother);
                ContactProfile.this.setResult(210, anotherPersonInfo);
                ContactProfile.this.finish();
                */
            }
        });
        return view;
    }
    /*
    @Override
    public void onBackPressed(){
        Intent doneViewing=new Intent();
        //doneViewing.putExtra("photo",this.encodedPhoto);
        //Log.e("Photo Trace","photo send back to main");
        doneViewing.putExtra("name",this.name);
        ContactProfile.this.setResult(201,doneViewing);
        ContactProfile.this.finish();
    }
    */
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("time_key", mTime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            this.viewPhotoLarge = rawPhoto;
            //this.encodedPhoto = BitmapToBytes(photo);
            Log.e("Photo","Photo taken and resized.");
            Person newFriend = new Person();
            newFriend.name = this.name;
            newFriend.setPhoto(this.viewPhoto);
            newFriend.setRawPhoto(this.viewPhotoLarge);
            repo.insert(newFriend);
            this.hasPhoto = true;
            Log.e("SQLite","Added photo for " + this.name);
        } else {
            Toast.makeText(this.getActivity(),
                    "Profile photo not added", Toast.LENGTH_SHORT).show();
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

    public void zoomImage(final View thumbView, Bitmap photo){
        Log.e("Zoomer","Zoomer called");
        final int duration = 200; // ms
        if (mCurrentAnimator != null){
            mCurrentAnimator.cancel();
        }

        final ImageView expanded_image = (ImageView) this.view.findViewById(R.id.photoZoomed);
        expanded_image.setImageBitmap(photo);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        //RelativeLayout container = (RelativeLayout) findViewById(R.id.background);


        thumbView.getGlobalVisibleRect(startBounds);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.view.findViewById(R.id.container).getGlobalVisibleRect(finalBounds);
        }
        else
        {
            this.view.findViewById(R.id.container).getGlobalVisibleRect(finalBounds);
        }
        //startBounds.offset(-globalOffset.x,-globalOffset.y);
        //finalBounds.offset(-globalOffset.x,-globalOffset.y);

        // for test only
        startBounds.top -= 210;
        startBounds.bottom -= 210;
        finalBounds.top -= 210;
        finalBounds.bottom -=210;

        float startScale;

        if((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()){
            startScale  = (float) startBounds.width() / startBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        }
        else{
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expanded_image.setVisibility(View.VISIBLE);
        expanded_image.setPivotX(0f);
        expanded_image.setPivotY(0f);
        view.findViewById(R.id.background).setVisibility(View.INVISIBLE);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expanded_image, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expanded_image, View.Y,startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expanded_image, View.SCALE_X,startScale, 1f)).with(ObjectAnimator.ofFloat(expanded_image,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();

        final float startScaleFinal = startScale;
        expanded_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.findViewById(R.id.background).setVisibility(View.VISIBLE);
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(expanded_image, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expanded_image, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(expanded_image, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(expanded_image, View.SCALE_Y, startScaleFinal));
                set.setDuration(duration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expanded_image.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expanded_image.setVisibility(View.GONE);
                    }
                });
                set.start();
            }
        });
    }

    public void saveData() {

        // Now Save contactbook(contacts and relationships)
        List<Map<String, String>> data=this.contactbook;
        String key="contactbook saves";
        JSONArray mJsonArray = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> itemMap = data.get(i);
            Iterator<Map.Entry<String, String>> iterator = itemMap.entrySet().iterator();
            JSONObject object = new JSONObject();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) { }
            }
            mJsonArray.put(object);
        }
        SharedPreferences sp = getActivity().getSharedPreferences("contact book", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, mJsonArray.toString());
        editor.commit();
    }

    public void loadData() {
        // Now Restore data including contactbook(contacts and relationships)
        String key="contactbook saves";
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        SharedPreferences sp = getActivity().getSharedPreferences("contact book", Context.MODE_PRIVATE);
        String result = sp.getString(key, "");
        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                Map<String, String> itemMap = new HashMap<String, String>();
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        itemMap.put(name, value);
                    }
                }
                data.add(itemMap);
            }
        } catch (JSONException e) { }
        this.contactbook=data;
    }
}