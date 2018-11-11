package com.example.detch.projjintang_daily_path;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CheckIn extends AppCompatActivity {
    private TextView showCheckinLongitude;
    private TextView showCheckinLatitude;
    private TextView showCheckinAddress;
    private EditText addNewNameForCheckin;
    private Button addButtonToCheckin;

    private String checkinLongitude;
    private String checkinLatitude;
    private String checkinAddress;
    private String checkinName;
    private String checkinTime;
    private List<HashMap<String, String>> saves;
    private List<HashMap<String, String>> adapterData;
    private boolean isInRangeOfHistory = false;
    private GeoRepo db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        this.showCheckinLongitude = (TextView)findViewById(R.id.add_txt_longitude_show);
        this.showCheckinLatitude = (TextView)findViewById(R.id.add_txt_latitude_show);
        this.showCheckinAddress = (TextView)findViewById(R.id.add_txt_address_show);
        this.addNewNameForCheckin = (EditText)findViewById(R.id.add_input_name);
        this.addButtonToCheckin = (Button)findViewById(R.id.add_btn_add_location);
        this.db = new GeoRepo(getApplicationContext());

        // LOAD DATA
        Intent  intent = getIntent();
        checkinLongitude = intent.getStringExtra("lon");
        checkinLatitude = intent.getStringExtra("lat");
        checkinAddress = intent.getStringExtra("addr");
        this.loadData();
        this.refreshAdapterData();
        this.checkinName = this.isInRangeOf(this.checkinLatitude, this.checkinLongitude, this.saves);
        this.addNewNameForCheckin.setText(this.checkinName);
        if (this.checkinName.length() > 0) {
            // Current Checkin Place is in range of history (30m)
            this.isInRangeOfHistory = true;
            this.addNewNameForCheckin.setFocusable(false);
            this.addNewNameForCheckin.setFocusableInTouchMode(false); // Set EditText no longer editable
        }

        // INITIALIZE UI
        this.showCheckinLatitude.setText(checkinLatitude);
        this.showCheckinLongitude.setText(checkinLongitude);
        this.showCheckinAddress.setText(checkinAddress);


        // SPECIFY LISTENERS
        this.addButtonToCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkinName = addNewNameForCheckin.getText().toString();
                checkinTime = currentTimeInFormat();
                if (checkinName.length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please specify a name before checkin", Toast.LENGTH_SHORT).show();
                } else {
                    // CHECKIN
                    HashMap<String, String> checkinData = new HashMap<String, String>();
                    checkinData.put("name", checkinName);
                    checkinData.put("time", checkinTime);
                    checkinData.put("addr", checkinAddress);
                    checkinData.put("lat", checkinLatitude);
                    checkinData.put("lon", checkinLongitude);
                    checkinData.put("mode", "checkin"); // Mode: checkin // Other modes: "bookmark"
                    GeoRepo.Geo geo = new GeoRepo.Geo(checkinName, checkinAddress, checkinLatitude, checkinLongitude, checkinTime, "checkin");
                    db.insert(geo);
                    saves.add(checkinData);
                    refreshAdapterData();
                    //saveData(getApplicationContext());
                    //viewHistoryCheckinsAdapter.notifyDataSetChanged();
                    Log.i("Checkin Page", "Checkin complete for " + checkinName);

                    CheckIn.this.setResult(10010, new Intent()); // Checkin success
                    CheckIn.this.finish();
                }
            }
        });

        this.addNewNameForCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInRangeOfHistory) {
                    Toast.makeText(getApplicationContext(), "You are in 30-meters range of history checkin", Toast.LENGTH_LONG).show();
                } else {

                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //this.saveData(this);
    }

    @Override
    public void onBackPressed() {
        //this.saveData(this);
        this.setResult(10000, new Intent()); // Checkin aborted
        this.finish();
    }

    private String isInRangeOf(String lat, String lon, List<HashMap<String, String >> history) {
        final double EARTH_RADIUS = 6378137;// Earth radius
        double thisLat = Double.valueOf(lat);
        double thisLon = Double.valueOf(lon);
        for (Map<String, String> eachHistory : history) {
            double hisLat = Double.valueOf(eachHistory.get("lat"));
            double hisLon = Double.valueOf(eachHistory.get("lon"));
            double ratLad1 = hisLat * Math.PI / 180.0;
            double ratLad2 = thisLat * Math.PI / 180.0;
            double latRadDif = ratLad1 - ratLad2; // Latitudes radius difference
            double lonRadDif = (hisLon * Math.PI / 180.0) - (thisLon * Math.PI / 180.0); // Longitudes radius difference
            double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latRadDif / 2), 2) + Math.cos(ratLad1) * Math.cos(ratLad2) * Math.pow(Math.sin(lonRadDif / 2), 2)));
            double distanceInMeter = distance * EARTH_RADIUS;
            if (distanceInMeter <= 30.0) {
                return eachHistory.get("name");
            }
        }
        return "";
    }

    private String currentTimeInFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// Time format
        return df.format(new Date());// Transfer timestamp into format
    }

    public void loadData() {
        // RESTORE GEO DATA FROM SQLITE DB
        this.saves = db.getAll();
    }

    private void refreshAdapterData() {
        this.adapterData = new ArrayList();
        for (HashMap<String, String> eachSave : this.saves) {
            HashMap<String, String> eachAdapterData = new HashMap<String, String>();
            eachAdapterData.put("name", eachSave.get("name"));
            eachAdapterData.put("addr", eachSave.get("addr"));
            eachAdapterData.put("time", eachSave.get("time"));
            eachAdapterData.put("lonlat", eachSave.get("lon") + ", " + eachSave.get("lat"));
            this.adapterData.add(eachAdapterData);
        }
    }
}
