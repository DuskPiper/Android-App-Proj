package com.example.detch.projjiaxing_contacts_app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsList extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button del_button;
    Button add_button;
    ListView main_list;
    List<Map<String,String >> contactbook = new ArrayList<Map<String,String >>();//contacts and their relationships are saved here
    static List<Integer> to_delete = new ArrayList<Integer>();//index to delete
    ContactListMainListAdapter contactListAdapter;
    ArrayList<String> namelist = new ArrayList<String>();
    private OnFragmentInteractionListener mListener;
    String mTime;

    public ContactsList() {}

    public static ContactsList newInstance(){//ArrayList<String> namelist) {
        ContactsList newFragment = new ContactsList();
        //Bundle bundle = new Bundle();
        //bundle.putStringArrayList("namelist", namelist);
        //newFragment.setArguments(bundle);
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
        super.onCreate(savedInstanceState);
        this.loadData();
        for (Map<String, String> contact : contactbook) {
            this.namelist.add(contact.get("name"));
        }
        //setContentView(R.layout.activity_contacts_list);
        View view=inflater.inflate(R.layout.activity_contacts_list, container, false);
        add_button = (Button) view.findViewById(R.id.addButton);
        del_button = (Button) view.findViewById(R.id.delButton);
        main_list = (ListView) view.findViewById(R.id.listOfContacts);
        contactListAdapter = new ContactListMainListAdapter(
                getActivity(),contactbook,R.id.deleterBox,R.layout.contact_list_per_item,new String[]{"name"},new int[]{R.id.contactName});
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
                saveData();
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
                /*
                Bundle sendnames=new Bundle();
                sendnames.putStringArrayList("names",names);
                Intent toAddNewContact=new Intent(ContactsList.this,AddNewContact.class);
                toAddNewContact.putExtras(sendnames);
                startActivityForResult(toAddNewContact,100);//+new contact
                saveData();
                */
                if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    AddNewContact fragment_2 = AddNewContact.newInstance(names);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment_2);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                else if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    AddNewContact fragment_2 = AddNewContact.newInstance(names);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container2, fragment_2);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });

        main_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Map<String,String> personToShow = contactbook.get(position);
                String nameToShow = personToShow.get("name");
                String phoneToShow = personToShow.get("phone");
                String relationshipToShow = personToShow.get("relationships");
                /*
                Intent personInfo = new Intent(ContactsList.this,ContactProfile.class);
                personInfo.putExtra("name", nameToShow);
                personInfo.putExtra("phone", phoneToShow);
                personInfo.putExtra("relationships", relationshipToShow);
                startActivityForResult(personInfo,200);
                */
                if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ContactProfile fragment_3 = ContactProfile.newInstance(nameToShow, phoneToShow, relationshipToShow);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container2, fragment_3);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    //return false;
                }
                else if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ContactProfile fragment_3 = ContactProfile.newInstance(nameToShow, phoneToShow, relationshipToShow);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment_3);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("time_key", mTime);
        saveData();
    }
    @Override
    public void onPause(){
        super.onPause();
        saveData();
    }
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveData();
    }

    /*
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
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
                saveData(ContactsList.this);
                break;
            case 210:
                // Done watching profile details but need to show another guy
                saveData(ContactsList.this);
                Map<String,String> personToShow = new HashMap<String, String>();
                String nameToShow = data.getStringExtra("anotherName");
                for (Map<String, String> findPersonForShow : this.contactbook) {
                    if (findPersonForShow.get("name").equals(nameToShow)) {
                        personToShow = findPersonForShow;
                        break;
                    }
                }
                if (personToShow.isEmpty()) {
                    Log.e("Show profile failure", "Name not found");
                    break;
                }
                Intent personInfo = new Intent(ContactsList.this,ContactProfile.class);
                personInfo.putExtra("name", nameToShow);
                personInfo.putExtra("phone", personToShow.get("phone"));
                personInfo.putExtra("relationships", personToShow.get("relationships"));
                //personInfo.putExtra("photo",photoToShow);
                startActivityForResult(personInfo,200);
                break;
            default:
                break;
        }


    }
    */

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

