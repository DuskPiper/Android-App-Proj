package com.example.detch.projjiaxing_contacts_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewContact extends Activity {
    Button addContact;
    EditText newName;
    EditText newPhone;
    ListView newRelationships;
    ArrayList<String> names;
    ArrayList<String> newRelationshipNames;
    ArrayList<Map<String,String>> liteContactBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);

        newRelationshipNames=new ArrayList<String>();
        addContact=(Button)findViewById(R.id.addNewContactButton);
        newName=(EditText)findViewById(R.id.addNewNameBox);
        newPhone=(EditText)findViewById(R.id.addNewPhoneBox);
        newRelationships=(ListView)findViewById(R.id.addNewRelationshipList);
        names=this.getIntent().getExtras().getStringArrayList("names");
        liteContactBook=new ArrayList<Map<String,String>>();
        for(int i=0;i<names.size();i++){
            Map<String,String> person=new HashMap<String,String>();
            person.put("name",names.get(i));
            liteContactBook.add(person);
        }
        AddRelationshipAdapter addRelationshipAdapter=new AddRelationshipAdapter(AddNewContact.this,
                liteContactBook,R.id.adderCheckbox,R.layout.relationship_with_checkbox_per_item,new String[]{"name"},new int[]{R.id.addRelationshipName});
        newRelationships.setAdapter(addRelationshipAdapter);
        addRelationshipAdapter.notifyDataSetChanged();

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addedName=newName.getText().toString();
                String addedPhone=newPhone.getText().toString();
                if(addedName.length()<1){
                    Toast.makeText(AddNewContact.this.getApplicationContext(),
                            "Plz fill the name",Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle sendBackNewContact =new Bundle();
                sendBackNewContact.putStringArrayList("relationship",newRelationshipNames);
                sendBackNewContact.putString("name",addedName);
                sendBackNewContact.putString("phone",addedPhone);
                Intent addedContact=new Intent();
                addedContact.putExtras(sendBackNewContact);
                AddNewContact.this.setResult(101,addedContact);
                AddNewContact.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent cancelAdding=new Intent();
        AddNewContact.this.setResult(102,cancelAdding);
        AddNewContact.this.finish();
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

}
