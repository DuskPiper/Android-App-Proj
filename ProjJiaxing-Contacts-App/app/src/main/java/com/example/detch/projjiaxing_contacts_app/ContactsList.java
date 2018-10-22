package com.example.detch.projjiaxing_contacts_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.*;



public class ContactsList extends AppCompatActivity {
    Button del_button;
    Button add_button;
    ListView main_list;
    List<Map<String,String >> contactbook = new ArrayList<Map<String,String >>();//contacts and their relationships are saved here
    static List<Integer> to_delete = new ArrayList<Integer>();//index to delete
    ContactListMainListAdapter contactListAdapter;
    //Map<String,byte[]> contactAlbum = new HashMap<String, byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        add_button = (Button) findViewById(R.id.addButton);
        del_button = (Button) findViewById(R.id.delButton);
        main_list = (ListView) findViewById(R.id.listOfContacts);
        loadData(ContactsList.this);
        contactListAdapter = new ContactListMainListAdapter(
                ContactsList.this,contactbook,R.id.deleterBox,R.layout.contact_list_per_item,new String[]{"name"},new int[]{R.id.contactName});
        main_list.setAdapter(contactListAdapter);
        contactListAdapter.notifyDataSetChanged();

        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //now remove
                for(int i=to_delete.size()-1;i>=0;i--){
                    int now_delete=to_delete.get(i);
                    contactbook.remove(now_delete);
                    Log.e("contacts size",Integer.toString(contactbook.size()));
                }
                contactListAdapter.notifyDataSetChanged();
                to_delete.clear();
                //now clear checkboxes
                CheckBox cb;
                for(int i=0;i<main_list.getChildCount();i++){
                    cb=(CheckBox)main_list.getChildAt(i).findViewById(R.id.deleterBox);
                    cb.setChecked(false);
                }
                contactListAdapter.notifyDataSetChanged();
                saveData(ContactsList.this);
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> names=new ArrayList<String>();
                for(int i=0;i<contactbook.size();i++){
                    String name=contactbook.get(i).get("name");
                    names.add(name);
                }
                Bundle sendnames=new Bundle();
                sendnames.putStringArrayList("names",names);
                Intent toAddNewContact=new Intent(ContactsList.this,AddNewContact.class);
                toAddNewContact.putExtras(sendnames);
                startActivityForResult(toAddNewContact,100);//+new contact
                saveData(ContactsList.this);
            }
        });

        main_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Map<String,String> personToShow = contactbook.get(position);
                //byte[] photoToShow;
                //if (contactAlbum.containsKey(nameToShow)) {
                //    photoToShow = contactAlbum.get(nameToShow);
                //    Log.e("Photo Trace","found photo for "+nameToShow+", sending to profile");
                //} else {
                //    Log.e("Photo Trace","cannot find photo for "+nameToShow+"using default");
                //    Bitmap nullPhoto = BitmapFactory.decodeResource(getResources(),R.drawable.nullphoto);
                //    photoToShow = BitmapToBytes(nullPhoto);
                //    contactAlbum.put(nameToShow,photoToShow);
                //}
                String nameToShow = personToShow.get("name");
                Intent personInfo = new Intent(ContactsList.this,ContactProfile.class);
                personInfo.putExtra("name",nameToShow);
                personInfo.putExtra("phone",personToShow.get("phone"));
                personInfo.putExtra("relationships",personToShow.get("relationships"));
                //personInfo.putExtra("photo",photoToShow);
                startActivityForResult(personInfo,200);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        saveData(ContactsList.this);
    }
    @Override
    protected void onPause(){
        super.onPause();
        saveData(ContactsList.this);
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        //super.onActivityResult();
        this.to_delete.clear();
        switch(resultCode){
            case 101:
                // Case: just added new contact
                // Now fetch intent data
                Bundle newlyAdded=data.getExtras();
                String newName=newlyAdded.getString("name");
                String newPhone=newlyAdded.getString("phone");
                ArrayList<String> newRelationship=newlyAdded.getStringArrayList("relationship");
                StringBuffer relationshipBuffer=new StringBuffer();
                for(int i=0;i<newRelationship.size();i++){
                    relationshipBuffer.append(newRelationship.get(i));
                    relationshipBuffer.append(";");
                }
                if(relationshipBuffer.length()>0){
                    relationshipBuffer.deleteCharAt(relationshipBuffer.length()-1);
                }
                // Now create a new object(person) for contactbook
                Map<String,String> newPerson=new HashMap<String, String>();
                newPerson.put("name",newName);
                newPerson.put("phone",newPhone);
                newPerson.put("relationships",newRelationship.toString());
                this.contactbook.add(newPerson);
                // Now add this new person's relationship to existing contacts (two-way relationship)
                for(int i=0;i<newRelationship.size();i++){
                    String oldFriendName = newRelationship.get(i);
                    // for (Map<String,String> oldFriend : this.contactbook){
                    for (int j=0;j<this.contactbook.size();j++){
                        Map<String,String> oldFriend = this.contactbook.get(j);
                        if (oldFriend.get("name").equals(oldFriendName)) {
                            String updatedRelationships = new String();
                            if (oldFriend.get("relationships").length()<3){// No relationship yet
                                updatedRelationships = "["+newName+"]";
                            } else {// Append newName to end of String
                                StringBuffer oldRelationshipBuffer = new StringBuffer(oldFriend.get("relationships"));
                                oldRelationshipBuffer.deleteCharAt(oldRelationshipBuffer.length()-1);
                                updatedRelationships = oldRelationshipBuffer + ", " + newName + "]";
                            }
                            Log.e("Update old relationship"+oldFriendName,"Now relation:"+updatedRelationships);
                            oldFriend.put("relationships",updatedRelationships);
                            this.contactbook.set(j,oldFriend);// Update oldFriend back
                        }
                    }
                }
                // Now do some wrap-ups
                Log.e("Activity Jump","Added new contact");
                Toast.makeText(ContactsList.this.getApplicationContext(),
                        "Added "+newName,Toast.LENGTH_SHORT).show();
                this.contactListAdapter.notifyDataSetChanged();
                saveData(ContactsList.this);
                break;
            case 102:
                // Canceled adding contact
                Toast.makeText(ContactsList.this.getApplicationContext(),
                        "Canceled adding",Toast.LENGTH_SHORT).show();
                break;
            case 201:
                // Done watching profile details
                //byte[] encodedPhoto = data.getByteArrayExtra("photo");
                //String viewedName = data.getStringExtra("name");
                //this.contactAlbum.put(viewedName,encodedPhoto);
                Log.d("Main result", "Safely back from profile viewing.");
                saveData(ContactsList.this);
                break;
            default:
                break;
        }


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
        /*
        // Now encode contactAlbum photo(byte[]) into string(Base64) and Save
        Map<String, String> encodedAlbum = new HashMap<String, String>();
        List<Map<String,String>> data2 = new ArrayList<Map<String, String>>();
        for (String decodedAlbumkey : this.contactAlbum.keySet()) {
            String encodedAlbumPhoto = BytesToBase64(this.contactAlbum.get(decodedAlbumkey));
            encodedAlbum.put(decodedAlbumkey,encodedAlbumPhoto);
        }
        String key2 = "contact album saves";
        JSONArray mJsonArray2 = new JSONArray();
        Iterator<Map.Entry<String, String>> iterator = encodedAlbum.entrySet().iterator();
        JSONObject object2 = new JSONObject();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                object2.put(entry.getKey(), entry.getValue());
                Log.e("Photo Trace","saving photo for "+entry.getKey());
            } catch (JSONException e) { }
        }
        mJsonArray2.put(object2);
        SharedPreferences sp2 = context.getSharedPreferences("contact album", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sp2.edit();
        editor2.putString(key2, mJsonArray2.toString());
        editor2.commit();
        */
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
        /*
        // Now restore contact album
        String key2 = "contact album saves";
        Map<String, String> encodedAlbum = new HashMap<String, String>();
        SharedPreferences sp2 = context.getSharedPreferences("contact album", Context.MODE_PRIVATE);
        String result2 = sp2.getString(key2, "");
        try {
            JSONArray array2 = new JSONArray(result2);
            JSONObject itemObject2 = array2.getJSONObject(0);
            Map<String, String> itemMap2 = new HashMap<String, String>();
            JSONArray names2 = itemObject2.names();
            if (names2 != null) {
                for (int j = 0; j < names2.length(); j++) {
                    String name2 = names2.getString(j);
                    String value2 = itemObject2.getString(name2);
                    itemMap2.put(name2, value2);
                    Log.e("Photo Trace","laoding photo for "+name2);
                }
            }
            encodedAlbum = itemMap2;
        } catch (JSONException e) { }
        this.contactAlbum.clear();
        for (String encodedAlbumKey : encodedAlbum.keySet()) {
            byte[] decodedAlbumPhoto = Base64ToBytes(encodedAlbum.get(encodedAlbumKey));
            this.contactAlbum.put(encodedAlbumKey,decodedAlbumPhoto);
        }
        */
    }

    public byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public String BytesToBase64(byte[] bt) {
        return Base64.encodeToString(bt,Base64.DEFAULT);
    }

    public byte[] Base64ToBytes(String st) {
        return Base64.decode(st,Base64.NO_WRAP);
    }

    public class ContactListMainListAdapter extends SimpleAdapter {
        List<Map<String, String>> listmaps;
        private int myCheckBoxId;

        public ContactListMainListAdapter(Context context, List<Map<String, String>> list,int checkBoxId, int resource, String[] from, int[] to) {
            super(context, list, resource, from, to);
            this.listmaps = list;
            this.myCheckBoxId=checkBoxId;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final CheckBox deleter = (CheckBox) view.findViewById(R.id.deleterBox);
            deleter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        ContactsList.to_delete.add(position);
                        Collections.sort(ContactsList.to_delete);
                    }
                    else{
                        int index=ContactsList.to_delete.indexOf(position);
                        if(index>-1){
                            ContactsList.to_delete.remove(new Integer(position));
                        }
                    }
                }
            });
            return view;
        }

        @Override
        public int getCount() {
            return listmaps.size();
        }
    }

}

