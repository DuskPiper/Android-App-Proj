package com.example.detch.projjiaxing_contacts_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class ContactsList extends AppCompatActivity {
    Button del_button;
    Button add_button;
    ListView main_list;
    /*Map<String, String> contacts = new HashMap();
    Map<String, String> relationships = new HashMap();*/
    List<Map<String,String >> contactbook = new ArrayList<Map<String,String >>();//contacts and their relationships are saved here
    static List<Integer> to_delete = new ArrayList<Integer>();
    ContactListMainListAdapter contactListAdapter;

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
                Intent personInfo = new Intent(ContactsList.this,ContactProfile.class);
                //Bundle infoBundle=new Bundle();
                personInfo.putExtra("name",personToShow.get("name"));
                personInfo.putExtra("phone",personToShow.get("phone"));
                personInfo.putExtra("relationships",personToShow.get("relationships"));
                startActivityForResult(personInfo,200);
                //startActivity(personInfo);
                //saveData(ContactsList.this);
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
        if(resultCode==101){
            //added new contact
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
            Map<String,String> newPerson=new HashMap<String, String>();
            newPerson.put("name",newName);
            newPerson.put("phone",newPhone);
            newPerson.put("relationships",newRelationship.toString());
            this.contactbook.add(newPerson);
            Log.e("Activity Jump","Added new contact");
            Toast.makeText(ContactsList.this.getApplicationContext(),
                    "Added "+newName,Toast.LENGTH_SHORT).show();
            this.contactListAdapter.notifyDataSetChanged();
            saveData(ContactsList.this);
        }
        else if(requestCode==102){
            //canceled adding contact
            Toast.makeText(ContactsList.this.getApplicationContext(),
                    "Canceled adding",Toast.LENGTH_SHORT).show();
        }
        else if(requestCode==201){
            //done watching profile details
        }
        else{}

    }


    public void saveData(Context context) {
        //save contactbook(contacts and relationships)
        List<Map<String, String>> data=this.contactbook;
        String key="saves";
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
        SharedPreferences sp = context.getSharedPreferences("finals", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, mJsonArray.toString());
        editor.commit();
    }

    public void loadData(Context context) {
        //restore contactbook(contacts and relationships)
        String key="saves";
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        SharedPreferences sp = context.getSharedPreferences("finals", Context.MODE_PRIVATE);
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

