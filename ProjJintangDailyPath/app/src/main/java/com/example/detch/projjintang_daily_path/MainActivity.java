package com.example.detch.projjintang_daily_path;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private TextView showCurrentLongitude;
    private TextView showCurrentLatitude;
    private TextView showCurrentAddress;
    private TextView showCurrentStatus;
    private Button checkinButton;
    private Button locateButton;
    private Button myMapButton;
    private String currentLongitude;
    private String currentLatitude;
    private String currentAddress;
    private String locationProvider;
    private boolean hasLocation = false;

    private LocationManager locationManager;
    private Geocoder geocoder;
    private int Y_PERMISSIONS_REQUEST_READ_CONTACTS = 998;

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

        this.currentAddress = "";
        this.currentLatitude = "";
        this.currentLongitude = "";
        geocoder = new Geocoder(this);

        getLocation();


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
                    startCheckinIntent.putExtra("long", currentLongitude);
                    startCheckinIntent.putExtra("addr", currentAddress);
                    startActivityForResult(startCheckinIntent, 100);
                } else {
                    Log.e("Checkin location","Insufficient data, canceled.");
                }

            }
        });
        // ToDo: Add OnActivityResult
    }

    private void getLocation() {
        Context context = this;
        // Now initialize Location Provider
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Y_PERMISSIONS_REQUEST_READ_CONTACTS);
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
            } else {
                Log.e("Location Acquisition", "Not refreshed, awaiting for locationChangeListener");
                locationManager.requestLocationUpdates(locationProvider, 0, 0, this.locationListener);
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

}
