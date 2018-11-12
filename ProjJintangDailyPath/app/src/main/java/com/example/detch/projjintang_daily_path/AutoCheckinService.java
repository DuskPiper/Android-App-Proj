package com.example.detch.projjintang_daily_path;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCheckinService extends Service { /*implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {*/

    protected LocationManager locationManager;
    private Geocoder geocoder;
    private GeoRepo db;
    private List<HashMap<String, String>> saves;
    private String locationProvider;
    private boolean permissionGranted = false;
    //private double lastLat = 999;
    //private double lastLon = 999; // initialize with impossible parameter to avoid conflicts
    private double curLat = 40.5214256;
    private double curLon = -74.4612562;
    private int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 998;
    protected boolean looperDaemon = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new GeoRepo(this);
        loadData();
        geocoder = new Geocoder(this);

        checkPermissions();
        if (!permissionGranted) {
            Toast.makeText(getApplicationContext(), "Failure starting service: insufficient permission", Toast.LENGTH_SHORT).show();
            Log.e("Auto Checkin Service","Failed to start, insufficient permission");
        } else {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            /*apiClient.connect();
            if (apiClient.isConnected()) {
                refreshLocation();
            }*/
            // DO THE MAIN WORK
            final Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //showNotification();
                    int secSinceCheckin = 0; // time since last auto checkin (sec)
                    int looper = 0;
                    double latSinceCheckin = curLat;
                    double lonSinceCheckin = curLon;
                    while (looper <= 1080 && looperDaemon) { // 3 Hours auto-close
                        Log.e("Auto Checkin Loop", "Looping #" + Integer.toString(looper));
                        looper += 1;
                        refreshLocation();
                        double distance = distanceInMeter(curLat, curLon, latSinceCheckin, lonSinceCheckin);
                        if (distance >= 100 || secSinceCheckin >= 20) {
                            Log.d("Auto Checkin Loop", "Checkin requirement triggered");
                            latSinceCheckin = curLat;
                            lonSinceCheckin = curLon;
                            secSinceCheckin = 0;
                            autoCheckin(curLat, curLon);
                            // BROADCAST
                            Intent checkedIn = new Intent();
                            checkedIn.setAction("piper-dailypath-auto-checkin");
                            sendBroadcast(checkedIn);
                        }

                        // SLEEP FOR 10 SEC
                        secSinceCheckin += 10;
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    stopSelf();
                }
            });
            mainThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("Auto Checkin", "Destroy called, stopping service...");
        looperDaemon = false;
        //stopSelf();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void refreshLocation() {
        // Now initialize Location Provider
        if (permissionGranted) {
            //Permission is granted
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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) {
                    curLat = location.getLatitude();
                    curLon = location.getLongitude();
                    saveSPData(); // Save last location data
                } else {
                    Log.e("Auto Checkin Service", "Not refreshed location, awaiting for locationChangeListener");
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failure refreshing location: insufficient permission", Toast.LENGTH_SHORT).show();
            Log.e("Auto Checkin Service","Failure refreshing location: insufficient permission");
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;
        }
    }

    public void loadData() {
        // RESTORE GEO DATA FROM SQLITE DB
        this.saves = db.getAll();
    }

    private void saveSPData() {
        // SAVE DATA TO SHARED PREFERENCES
        Context context = getApplicationContext();
        String key = "saves";
        JSONObject saves = new JSONObject();
        try {
            saves.put("lastLat", Double.toString(curLat));
            saves.put("lastLon", Double.toString(curLon));
        } catch (JSONException e) {
            Log.e("Save SP Data", "JSON Error");
        }
        SharedPreferences sp = context.getSharedPreferences("saves-sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, saves.toString());
        editor.commit();
    }

    private double distanceInMeter(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 > 990 || lat2 > 990 || lon1 > 990 || lon2 > 990) {
            // Invalid data
            return 0;
        }
        final double EARTH_RADIUS = 6378137;// Earth radius
        double ratLad1 = lat1 * Math.PI / 180.0;
        double ratLad2 = lat2 * Math.PI / 180.0;
        double latRadDif = ratLad1 - ratLad2; // Latitudes radius difference
        double lonRadDif = (lon1 * Math.PI / 180.0) - (lon2 * Math.PI / 180.0); // Longitudes radius difference
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latRadDif / 2), 2) + Math.cos(ratLad1) * Math.cos(ratLad2) * Math.pow(Math.sin(lonRadDif / 2), 2)));
        double distanceInMeter = distance * EARTH_RADIUS;
        return distanceInMeter;
    }

    private String isInRangeOf(double thisLat, double thisLon, List<HashMap<String, String >> history) {
        final double EARTH_RADIUS = 6378137;// Earth radius
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

    private void autoCheckin(double lat, double lon) {
        // CHECK IF IS IN RANGE OF EXISTING
        String checkinName = isInRangeOf(lat, lon, saves);
        if (checkinName.length() < 1) {
            checkinName = "Auto-Check-In";
        }
        // INITIALIZE OTHER DATA
        String checkinTime = currentTimeInFormat();
        String checkinAddress = "N/A";
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                checkinAddress = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Fail to obtain address from location");
            e.printStackTrace();
        }
        // CHECKIN
        HashMap<String, String> checkinData = new HashMap<String, String>();
        checkinData.put("name", checkinName);
        checkinData.put("time", checkinTime);
        checkinData.put("addr", checkinAddress);
        checkinData.put("lat", Double.toString(lat));
        checkinData.put("lon", Double.toString(lon));
        checkinData.put("mode", "checkin"); // Mode: checkin // Other modes: "bookmark"
        GeoRepo.Geo geo = new GeoRepo.Geo(checkinName, checkinAddress, Double.toString(lat), Double.toString(lon), checkinTime, "checkin");
        db.insert(geo);
        saves.add(checkinData);
        Log.i("Auto Checkin", "Checked in new location: " + checkinName);
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Auto-Checkin Running");
        mBuilder.setContentText("Auto checkin every 100-meters / 5-minutes");
        startForeground(1, mBuilder.build());
    }

}
