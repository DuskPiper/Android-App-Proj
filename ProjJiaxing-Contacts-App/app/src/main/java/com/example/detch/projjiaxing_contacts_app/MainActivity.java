package com.example.detch.projjiaxing_contacts_app;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        ContactsList.OnFragmentInteractionListener, ContactProfile.OnFragmentInteractionListener,
        AddNewContact.OnFragmentInteractionListener{

    //ArrayList<ListviewContactItem> listContact = new ArrayList<ListviewContactItem>();
    List<Map<String,String >> contactbook = new ArrayList<Map<String,String >>();
    ArrayList<String> namelist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*  LOAD DATA
        SharedPreferences sp = getSharedPreferences("shared",MODE_PRIVATE);
        int size = sp.getInt("size",0);
        for (int i=0;i<size;i++){
            String name = sp.getString(i+"name","");
            String num = sp.getString(i+"num","");
            ListviewContactItem tmp = new ListviewContactItem();
            tmp.setName(name);
            tmp.setPhone_num(num);
            Set<String> set=sp.getStringSet(i+"relations",null);
            ArrayList<String> relations = new ArrayList<String>();
            relations.addAll(set);
            tmp.setRelations(relations);
            String photo_string=sp.getString(i+"photo","");
            if (photo_string!="Null"){
                byte[] decodedByte = Base64.decode(photo_string,0);
                Bitmap photo = BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
                tmp.setPhoto(photo);
            }
            listContact.add(tmp);
        }*/
        this.loadData(this);
        for (Map<String, String> contact : contactbook) {
            this.namelist.add(contact.get("name"));
        }

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("info", "landscape");
            ContactsList fragment_1 = ContactsList.newInstance();
            FragmentManager fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container1, fragment_1);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            /*
            ContactProfile fragment_2 = ContactProfile.newInstance(listContact);
            FragmentManager fragmentManager2 = this.getFragmentManager();
            FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
            fragmentTransaction2.replace(R.id.fragment_container2, fragment_2);
            fragmentTransaction2.addToBackStack(null);
            fragmentTransaction2.commit();
            */
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("info", "portrait");
            ContactsList fragment_1 = ContactsList.newInstance();
            FragmentManager fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment_1);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void onFragmentInteraction(Uri uri){}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
            this.setContentView(R.layout.activity_main);
            ContactsList fragment_1 = ContactsList.newInstance();
            FragmentManager fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container1, fragment_1);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            /*
            ContactProfile fragment_2 = ContactProfile.newInstance(listContact);
            FragmentManager fragmentManager2 = this.getFragmentManager();
            FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
            fragmentTransaction2.replace(R.id.fragment_container2, fragment_2);
            fragmentTransaction2.addToBackStack(null);
            fragmentTransaction2.commit();
            */
        }else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            this.setContentView(R.layout.activity_main);
            ContactsList fragment_1 = ContactsList.newInstance();
            FragmentManager fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment_1);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop(){
        super.onStop();
        /* SAVE DATA
        SharedPreferences sp = getSharedPreferences("shared",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("size",listContact.size());
        for (int i=0;i<listContact.size();i++){
            String name = listContact.get(i).getContact_name();
            String num = listContact.get(i).getPhone_num();
            editor.putString(i+"name",name);
            editor.putString(i+"num",num);
            ArrayList<String> relations = listContact.get(i).getRelations();
            Set<String> set = new HashSet<String>();
            set.addAll(relations);
            editor.putStringSet(i+"relations",set);

            Bitmap photo = listContact.get(i).getPhoto();
            if (photo!=null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG,100, baos);
                byte[] b = baos.toByteArray();
                String imageEncoded= Base64.encodeToString(b,Base64.DEFAULT);
                editor.putString(i+"photo",imageEncoded);
            }
            else{
                editor.putString(i+"photo","Null");
            }
        }

        editor.commit();
        */

    }

    public void saveData(Context context) {

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
        SharedPreferences sp = context.getSharedPreferences("contact book", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, mJsonArray.toString());
        editor.commit();
    }

    public void loadData(Context context) {
        // Now Restore data including contactbook(contacts and relationships)
        String key="contactbook saves";
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        SharedPreferences sp = context.getSharedPreferences("contact book", Context.MODE_PRIVATE);
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

