package com.example.detch.projjiaxing_contacts_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    byte[] encodedPhoto;

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
        // Now set profile photo
        //Bitmap nullPhoto = BitmapFactory.decodeResource(getResources(),R.drawable.nullphoto);
        encodedPhoto = this.getIntent().getByteArrayExtra("photo");
        viewPhoto = BytesToBitmap(encodedPhoto);
        profilePhoto.setImageBitmap(viewPhoto);




        // Set listeners
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start camera for a photo
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent doneViewing=new Intent();
        doneViewing.putExtra("photo",this.encodedPhoto);
        Log.e("Photo Trace","photo send back to main");
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
            // Now show the photo (TEST)
            profilePhoto.setImageBitmap(photo);
            this.viewPhoto = photo;
            this.encodedPhoto = BitmapToBytes(photo);
            Log.e("Photo Trace","taken photo updated to ContactProfile.java");
        } else {
            Toast.makeText(this.getApplicationContext(),
                    "Profile photo not added",Toast.LENGTH_SHORT).show();
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
}
