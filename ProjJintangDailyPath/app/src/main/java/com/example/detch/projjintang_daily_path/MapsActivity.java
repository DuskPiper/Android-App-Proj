package com.example.detch.projjintang_daily_path;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
//import android.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<HashMap<String, String>> saves;
    private List<Marker> markers;
    private GeoRepo db;
    private String lastLat = "40.5214256";
    private String lastLon = "-74.4612562";
    private int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 998;
    private GeoRepo.Geo bookMark;
    private int bookmarkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        // Initialize Data
        this.db = new GeoRepo(getApplicationContext());
        this.markers = new ArrayList<Marker>();
        this.loadData();
        this.loadSPData();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        } else {
            //Permission is granted
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            if (mGoogleApiClient.isConnected()) {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(5000);
                mLocationRequest.setFastestInterval(3000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener)this);
            }
        }

        for (Map<String, String> location : this.saves) {
            LatLng point = new LatLng(Double.valueOf(location.get("lat")), Double.valueOf(location.get("lon")));
            Marker eachMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(location.get("name"))
                    .snippet(location.get("time"))
                    //.snippet(stringShortener(location.get("addr"), 15))
            );
            markers.add(eachMarker);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(this.lastLat), Double.valueOf(this.lastLon)), 16));
        // CoRE Building: 40.5214256, -74.4612562
        showInfoInRange(Double.valueOf(this.lastLat), Double.valueOf(this.lastLon), markers);

        // SET LISTENERS
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                Log.d("Google Map", "Map clicked");
                final EditText bookmarkNameET = new EditText(MapsActivity.this);
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("New Bookmark Name")
                        .setView(bookmarkNameET)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Marker bookmarkMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .snippet(currentTimeInFormat())
                                        .draggable(true)
                                        .title(bookmarkNameET.getText().toString()));
                                markers.add(bookmarkMarker);
                                bookMark = new GeoRepo.Geo(
                                        bookmarkNameET.getText().toString(),
                                        "User bookmark",
                                        Double.toString(latLng.latitude),
                                        Double.toString(latLng.longitude),
                                        currentTimeInFormat(),
                                        "bookmark");
                                bookmarkId = db.insert(bookMark);
                                bookMark.setId(bookmarkId);
                            }
                        }).setNegativeButton("CANCEL", null).show();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (bookMark != null) {
                    bookMark.lat = Double.toString(marker.getPosition().latitude);
                    bookMark.lon = Double.toString(marker.getPosition().longitude);
                    db.update(bookMark);
                    Toast.makeText(MapsActivity.this, "Bookmark position updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.setResult(20010, new Intent());
        this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 2
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 3
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Map Location Listener", "Location changed");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        showInfoInRange(location.getLatitude(), location.getLongitude(), markers);
    }
/*
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("Map Location Listener", "Status has changed");
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
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

    public String stringShortener(String st, int len) {
        if (st.length() <= len) {
            return st;
        } else {
            StringBuffer sb = new StringBuffer(st);
            return (sb.substring(0, len) + "...");
        }
    }

    private String currentTimeInFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// Time format
        return df.format(new Date());// Transfer timestamp into format
    }

    private void showInfoInRange(double thisLat, double thisLon, List<Marker> markers) {
        final double EARTH_RADIUS = 6378137;// Earth radius
        for (Marker eachMarker : markers) {
            double hisLat = eachMarker.getPosition().latitude;
            double hisLon = eachMarker.getPosition().longitude;
            double ratLad1 = hisLat * Math.PI / 180.0;
            double ratLad2 = thisLat * Math.PI / 180.0;
            double latRadDif = ratLad1 - ratLad2; // Latitudes radius difference
            double lonRadDif = (hisLon * Math.PI / 180.0) - (thisLon * Math.PI / 180.0); // Longitudes radius difference
            double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latRadDif / 2), 2) + Math.cos(ratLad1) * Math.cos(ratLad2) * Math.pow(Math.sin(lonRadDif / 2), 2)));
            double distanceInMeter = distance * EARTH_RADIUS;
            if (distanceInMeter <= 30.0) {
                eachMarker.showInfoWindow();
            } else {
                eachMarker.hideInfoWindow();
            }
        }
    }
}
