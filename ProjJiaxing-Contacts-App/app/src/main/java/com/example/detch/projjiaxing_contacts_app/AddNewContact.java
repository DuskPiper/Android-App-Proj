package com.example.detch.projjiaxing_contacts_app;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewContact extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    Button addContact;
    EditText newName;
    EditText newPhone;
    ListView newRelationships;
    ArrayList<String> names;
    ArrayList<String> newRelationshipNames;
    ArrayList<Map<String,String>> liteContactBook;
    List<Map<String,String >> contactbook = new ArrayList<Map<String,String >>();
    View view;
    String mTime;


    public AddNewContact() {}

    public static AddNewContact newInstance(ArrayList<String> namelist) {
        AddNewContact newFragment = new AddNewContact();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("names", namelist);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_new_contact);
        if (savedInstanceState != null) {
            // Restore last state
            mTime = savedInstanceState.getString("time_key");
        } else {
            mTime = "" + Calendar.getInstance().getTimeInMillis();
        }

        loadData();
        View view=inflater.inflate(R.layout.activity_add_new_contact, container, false);
        Bundle bundle=getArguments();
        newRelationshipNames=new ArrayList<String>();
        addContact=(Button)view.findViewById(R.id.addNewContactButton);
        newName=(EditText)view.findViewById(R.id.addNewNameBox);
        newPhone=(EditText)view.findViewById(R.id.addNewPhoneBox);
        newRelationships=(ListView)view.findViewById(R.id.addNewRelationshipList);
        names=bundle.getStringArrayList("names");
        liteContactBook=new ArrayList<Map<String,String>>();
        for(int i=0;i<names.size();i++){
            Map<String,String> person=new HashMap<String,String>();
            person.put("name",names.get(i));
            liteContactBook.add(person);
        }
        AddRelationshipAdapter addRelationshipAdapter=new AddRelationshipAdapter(getActivity(),
                liteContactBook,R.id.adderCheckbox,R.layout.relationship_with_checkbox_per_item,new String[]{"name"},new int[]{R.id.addRelationshipName});
        newRelationships.setAdapter(addRelationshipAdapter);
        addRelationshipAdapter.notifyDataSetChanged();

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addedName=newName.getText().toString();
                String addedPhone=newPhone.getText().toString();
                if(addedName.length()<1){
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Plz fill the name",Toast.LENGTH_SHORT).show();
                    return;
                }

                /*StringBuffer relationshipBuffer=new StringBuffer();
                for(int i=0;i<newRelationshipNames.size();i++){
                    relationshipBuffer.append(newRelationshipNames.get(i));
                    relationshipBuffer.append(";");
                }
                if(relationshipBuffer.length()>0){
                    relationshipBuffer.deleteCharAt(relationshipBuffer.length()-1);
                }*/
                // Now create a new object(person) for contactbook
                Map<String,String> newPerson=new HashMap<String, String>();
                newPerson.put("name",addedName);
                newPerson.put("phone",addedPhone);
                newPerson.put("relationships",newRelationshipNames.toString());
                contactbook.add(newPerson);
                saveData();
                /*
                Bundle sendBackNewContact =new Bundle();
                sendBackNewContact.putStringArrayList("relationship",newRelationshipNames);
                sendBackNewContact.putString("name",addedName);
                sendBackNewContact.putString("phone",addedPhone);
                Intent addedContact=new Intent();
                addedContact.putExtras(sendBackNewContact);
                AddNewContact.this.setResult(101,addedContact);
                AddNewContact.this.finish();
                */
                if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ContactsList fragment_1 = ContactsList.newInstance();
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment_1);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                else if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ContactsList fragment_1 = ContactsList.newInstance();
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container1, fragment_1);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();


                    newPerson.put("relationships",newRelationshipNames.toString());

                    ContactProfile fragment_3 = ContactProfile.newInstance(addedName, addedPhone, newRelationshipNames.toString());
                    FragmentManager fragmentManager3 = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction3 = fragmentManager3.beginTransaction();
                    fragmentTransaction3.replace(R.id.fragment_container2, fragment_3);
                    fragmentTransaction3.addToBackStack(null);
                    fragmentTransaction3.commit();
                }
            }
        });
        return view;
    }
    /*
    @Override
    public void onBackPressed(){
        Intent cancelAdding=new Intent();
        AddNewContact.this.setResult(102,cancelAdding);
        AddNewContact.this.finish();
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
        saveData();
    }

    public class AddRelationshipAdapter extends SimpleAdapter {
        List<Map<String, String>> listmaps;
        private int myCheckBoxId;

        public AddRelationshipAdapter(Context context, List<Map<String, String>> list, int checkBoxId, int resource, String[] from, int[] to) {
            super(context, list, resource, from, to);
            this.listmaps = list;
            this.myCheckBoxId=checkBoxId;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckBox deleter = (CheckBox) view.findViewById(R.id.adderCheckbox);
            deleter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String checkedName=AddNewContact.this.liteContactBook.get(position).get("name");
                    if(isChecked){
                        AddNewContact.this.newRelationshipNames.add(checkedName);
                    }
                    else{
                        AddNewContact.this.newRelationshipNames.remove(checkedName);
                    }
                    notifyDataSetChanged();
                }
            });
            return view;
        }

        @Override
        public int getCount() {
            return listmaps.size();
        }
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
