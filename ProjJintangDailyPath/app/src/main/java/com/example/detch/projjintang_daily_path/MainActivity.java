package com.example.detch.projjintang_daily_path;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private TextView showCurrentLongitude;
    private TextView showCurrentLatitude;
    private TextView showCurrentAddress;
    private TextView showCurrentStatus;
    private ListView showCheckedInPlaces;
    private Button checkinButton;
    private Button locateButton;
    private Button myMapButton;
    private String currentLongitude;
    private String currentLatitude;
    private String currentAddress;
    private String locationProvider;
    private boolean hasLocation = false;
    private List<HashMap<String, String>> saves; // Stores saved geopoints
    private List<HashMap<String, String>> adapterData;
    private SimpleAdapter viewHistoryCheckinsAdapter;

    private LocationManager locationManager;
    private Geocoder geocoder;
    private int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 998;
    private GeoRepo db;
    private String lastLat = "40.5214256";
    private String lastLon = "-74.4612562";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.showCurrentLongitude = (TextView)findViewById(R.id.main_txt_longitude_show);
        this.showCurrentLatitude = (TextView)findViewById(R.id.main_txt_latitude_show);
        this.showCurrentAddress = (TextView)findViewById(R.id.main_txt_address_show);
        this.showCurrentStatus = (TextView)findViewById(R.id.main_txt_current_status);
        this.checkinButton = (Button)findViewById(R.id.main_btn_checkin);
        this.locateButton = (Button)findViewById(R.id.main_btn_locate);
        this.myMapButton = (Button)findViewById(R.id.main_btn_my_map);
        this.showCheckedInPlaces = (ListView)findViewById(R.id.add_list_history_show);
        this.db = new GeoRepo(getApplicationContext());

        // LOAD DATA
        loadData();
        refreshAdapterData();
        this.currentAddress = "";
        this.currentLatitude = "";
        this.currentLongitude = "";
        geocoder = new Geocoder(this);
        getLocation();

        // INITIALIZE UI
        viewHistoryCheckinsAdapter = new SimpleAdapter(
                this, this.adapterData, R.layout.listview_history_checkins, new String[]{"name", "addr", "time", "lonlat"}, new int[]{R.id.listview_show_history_name, R.id.listview_show_history_address, R.id.listview_show_history_time, R.id.listview_show_history_longitude_latitude}
        );
        showCheckedInPlaces.setAdapter(viewHistoryCheckinsAdapter);
        viewHistoryCheckinsAdapter.notifyDataSetChanged();

        // SPECIFY LISTENERS
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasLocation) {
                    // Start Checkin Activity for checkin
                    Intent startCheckinIntent = new Intent(MainActivity.this, CheckIn.class);
                    startCheckinIntent.putExtra("lat", currentLatitude);
                    startCheckinIntent.putExtra("lon", currentLongitude);
                    startCheckinIntent.putExtra("addr", currentAddress);
                    startActivityForResult(startCheckinIntent, 100);
                } else {
                    Log.e("Checkin location","Insufficient data, canceled.");
                }

            }
        });

        myMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startMapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startMapIntent.putExtra("lat", currentLatitude);
                startMapIntent.putExtra("lon", currentLongitude);
                startActivityForResult(startMapIntent, 200);
            }
        });
    }

    private void getLocation() {
        Context context = this;
        // Now initialize Location Provider
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        } else {
            //Permission is granted
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
                Log.i("Location Provider","Network");
            } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
                Log.i("Location Provider","GPS");
            } else {
                Toast.makeText(this, "Failed to find Location Provider.", Toast.LENGTH_SHORT).show();
                Log.e("Location Provider","N/A");
                return;
            }
            // Now acquire location
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                showLocation(location);
                this.lastLat = Double.toString(location.getLatitude());
                this.lastLon = Double.toString(location.getLongitude());
                saveSPData(); // Save last location data
            } else {
                Log.e("Location Acquisition", "Not refreshed, awaiting for locationChangeListener");
                locationManager.requestLocationUpdates(locationProvider, 0, 0, this.locationListener);
                loadSPData(); // Load history location data
            }
        }
    }

    private void showLocation(Location location) {
        // Now receive location data and adapt it on screen
        this.hasLocation = true;
        this.currentLongitude = Double.toString(location.getLongitude());
        this.currentLatitude = Double.toString(location.getLatitude());
        this.showCurrentLongitude.setText(this.currentLongitude);
        this.showCurrentLatitude.setText(this.currentLatitude);
        this.showCurrentStatus.setText("Location found");
        // Now get address using Geocoder
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            // Note: for this time we only pick the most likely address
            if (addresses.size() > 0) {
                Address firstLikelyAddress = addresses.get(0);
                this.currentAddress = firstLikelyAddress.getAddressLine(0);
                this.showCurrentAddress.setText(this.currentAddress);
            } else {
                Log.e("Geocoder", "No valid address responded");
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Fail to obtain address from location");
            e.printStackTrace();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 998: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Location request","Granted");
                } else {
                    Log.e("Location request","Denied");
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BACK FROM CHECKIN
        switch (resultCode) {
            case 10000: // Checkin Canceled
                Toast.makeText(this, "Checkin Canceled", Toast.LENGTH_SHORT).show();
                Log.e("Back from Checkin Page", "Checkin canceled");
                break;
            case 10010: // Checkin Successful
                Toast.makeText(this, "Checkin Completed", Toast.LENGTH_SHORT).show();
                Log.i("Back from Checkin Page", "Checkin successful");
                loadData();
                this.refreshAdapterData();
                this.viewHistoryCheckinsAdapter.notifyDataSetChanged();
                break;
            case 20010: // Map view complete
                Log.i("Back from Map Page", "Done watching map");
                break;
            default:
                // Unregistered or unexpected result
                Log.wtf("Back from activity", "UNEXPECTED RESULT CODE");
        }
    }


    public void saveSPData() {
        // SAVE DATA TO SHARED PREFERENCES
        Context context = getApplicationContext();
        String key = "saves";
        JSONObject saves = new JSONObject();
        try {
            saves.put("lastLat", this.lastLat);
            saves.put("lastLon", this.lastLon);
        } catch (JSONException e) {
            Log.e("Ssave SP Data", "JSON Error");
        }
        SharedPreferences sp = context.getSharedPreferences("saves-sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, saves.toString());
        editor.commit();
    }

    public void loadSPData() {
        // RESTORE SAVE DATA FROM SHARED PREFERENCES
        Context context = getApplicationContext();
        String key = "saves";
        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        SharedPreferences sp = context.getSharedPreferences("saves-sp", Context.MODE_PRIVATE);
        String result = sp.getString(key, "");
        try {
            JSONObject saves = new JSONObject(result);
            this.lastLat = saves.getString("lastLat");
            this.lastLon = saves.getString("lastLon");
        } catch (JSONException e) {
            Log.e("Load SP Data", "JSON Error, reseting last location to default");
            this.lastLat = "40.5214256";
            this.lastLon = "-74.4612562";
        }
    }

    public void loadData() {
        // RESTORE GEO DATA FROM SQLITE DB
        this.saves = db.getAll();
        Log.d("Load Data", "Data Loaded, size = " + Integer.toString(this.saves.size()));
    }

    public void saveData(Context context) {
        // SAVE GEO DATA TO SQLITE DB
        // ToDo: add save all in GeoRepo
    }

    private void refreshAdapterData() {
        if (this.adapterData != null) {
            this.adapterData.clear();
        } else {
            this.adapterData = new ArrayList();
        }
        for (Map<String, String> eachSave : this.saves) {
            if (eachSave.get("mode").equals("checkin")) {
                HashMap<String, String> eachAdapterData = new HashMap<String, String>();
                eachAdapterData.put("name", eachSave.get("name"));
                eachAdapterData.put("addr", eachSave.get("addr"));
                eachAdapterData.put("time", eachSave.get("time"));
                eachAdapterData.put("lonlat", eachSave.get("lon") + ", " + eachSave.get("lat"));
                this.adapterData.add(eachAdapterData);
            }
        }
    }

}
