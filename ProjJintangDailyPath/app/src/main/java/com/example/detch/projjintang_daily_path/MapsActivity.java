package com.example.detch.projjintang_daily_path;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
// import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private List<HashMap<String, String>> saves;
    private GeoRepo db;
    private String lastLat = "40.5214256";
    private String lastLon = "-74.4612562";

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
        // Initialize Data
        this.db = new GeoRepo(getApplicationContext());
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

        for (Map<String, String> location : this.saves) {
            LatLng point = new LatLng(Double.valueOf(location.get("lat")), Double.valueOf(location.get("lon")));
            mMap.addMarker(new MarkerOptions().position(point).title(location.get("name")));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(this.lastLat), Double.valueOf(this.lastLon)), 16)); // CoRE Building: 40.5214256, -74.4612562
    }

    @Override
    public void onBackPressed() {
        this.setResult(20010, new Intent()); // Checkin aborted
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
}
