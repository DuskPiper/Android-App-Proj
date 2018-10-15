package com.example.detch.projjiaxing_contacts_app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactProfile extends AppCompatActivity {
    TextView viewName;
    TextView viewPhone;
    ListView viewRelationships;
    ArrayList<Map<String,String>> liteContactBook;
    String name;
    String phone;
    String relationshipsString;
    String[] relationshipNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        viewName=(TextView)findViewById(R.id.viewNameBox);
        viewPhone=(TextView)findViewById(R.id.viewPhoneBox);
        viewRelationships=(ListView)findViewById(R.id.viewRelationshipList);
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
        Log.e("DEBUG",Integer.toString(relationshipNames.length));
        SimpleAdapter viewRelationshipAdapter = new SimpleAdapter(
                ContactProfile.this,liteContactBook,R.layout.relationship_without_checkbox_per_item,new String[]{"name"},new int[]{R.id.watchRelationshipName});
        viewRelationships.setAdapter(viewRelationshipAdapter);
        viewRelationshipAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed(){
        Intent doneViewing=new Intent();
        ContactProfile.this.setResult(201,doneViewing);
        ContactProfile.this.finish();
    }
}
