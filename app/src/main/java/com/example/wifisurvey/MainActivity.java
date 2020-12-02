package com.example.wifisurvey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager = null;
    private LocationManager locationManager = null;
    private boolean scanReadyStatus = true;

    /**
     * Exports the contents of the database to a CSV file.
     */
    private void exportDatabaseCSV() {

    }

    /**
     * Destructively clears the Database. All contents are lost.
     *
     * TODO Add a warning prompt
     */
    private void clearDatabase() {
        Room.databaseBuilder(this, WifiSurveyDatabase.class, "WifiSurveyDatabase")
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * Switches activity to MapsActivity
     */
    private void toMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * The callback for when a Location request completes. Does not change indicator color.
     */
    private final Consumer<Location> gpsCallback = t -> {
        Log.d("WiFi Survey:addLocationPing", "Attempting to add Location: " + t.toString());
        LocationPing obs = new LocationPing(t);
        Log.d("WiFi Survey:addLocationPing", "Timestamp nanos: " + obs.getTimeSinceBootNanos());
        WifiSurveyDatabase db = WifiSurveyDatabase.getInstance(this);
        db.getLocationPingDao().insertLocationPing(obs);
    };

    /**
     * The callback for when a WiFi scan completes. Adds the results to the DB and changes
     * indicator to ready.
     */
    private final BroadcastReceiver wifiScanCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                for (ScanResult i : mScanResults)
                {
                    Log.d("WiFi Survey:wifiScanCallback", "Adding observation: " + i.toString());

                    WifiObservation obs = new WifiObservation(i);
                    WifiSurveyDatabase.getInstance(context).getWifiObservationDao().insertWifiObservation(obs);
                }

                setReadyIndicator(true);
                updateTextBox();
            }
        }
    };

    /**
     * Updates the big text view with the topp 100 wifi observations
     *
     * TODO Replace with recyclerview
     */
    private void updateTextBox() {
        List<WifiObservation> observations = WifiSurveyDatabase.getInstance(this)
                .getWifiObservationDao().get100WifiObservations();

        StringBuilder newText = new StringBuilder();
        for (WifiObservation obs : observations) {
            newText.append("SSID: ").append(obs.getSsid()).append("; Timestamp: ")
                    .append(obs.getTimeSinceBootMicros()).append("\n");
        }

        ((EditText)findViewById(R.id.outputTextBox)).setText(newText.toString());
    }

    /**
     * Changes the "Ready State Indicator".
     *
     * TODO Change indicator color
     *
     * @param status True means Ready, false means Wait
     */
    private void setReadyIndicator(boolean status) {
        Button button = findViewById(R.id.readyIndicator);

        if (status) {
            button.setText(getResources().getString(R.string.ready));
            scanReadyStatus = true;
        } else {
            button.setText(getResources().getString(R.string.wait));
            scanReadyStatus = false;
        }
    }

    /**
     * Launches the WiFi and GPS scans.
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    public void doScan()
    {
        //Register GPS callback
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null,
                ContextCompat.getMainExecutor(this), gpsCallback);
        //Run the scan
        wifiManager.startScan();
        setReadyIndicator(false);
    }

    /**
     * Checks permissions, acquires handles to Managers, registers WiFi scan callback, and
     * registers button click callbacks
     * @param savedInstanceState Currently only passed to super.
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 87);
        }
        if(checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 87);
        }
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 87);
        }

        scanReadyStatus = true;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //For GPS
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //For WiFi scans
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //Register WiFi scan callback
        registerReceiver(wifiScanCallback,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        ((EditText)findViewById(R.id.outputTextBox)).setHorizontallyScrolling(true);

        //Callback for Scan
        findViewById(R.id.scanButton).setOnClickListener(view -> doScan());

        //Callback for Export
        findViewById(R.id.exportButton).setOnClickListener(view -> exportDatabaseCSV());

        //Callback for Clear
        findViewById(R.id.clearButton).setOnClickListener(view -> clearDatabase());

        findViewById(R.id.toMapButton).setOnClickListener(view -> toMap());

    }

    /**
     * Does nothing yet.
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }
}