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
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager = null;
    private LocationManager locationManager = null;

    private boolean wifiScanInProgress = false;
    private boolean gpsScanInProgress = false;
    private boolean scanReadyStatus = true;

    private int currentScanId = 0;

    public void setGpsScanInProgress(boolean gpsScanInProgress) {
        this.gpsScanInProgress = gpsScanInProgress;

        //No scans in progress so set scanReadyStatus to true
        if (!this.gpsScanInProgress && !this.wifiScanInProgress) {
            setReadyIndicator(true);
        }

        if (this.gpsScanInProgress) {
            setReadyIndicator(false);
        }
    }

    public void setWifiScanInProgress(boolean wifiScanInProgress) {
        this.wifiScanInProgress = wifiScanInProgress;

        //No scans in progress so set scanReadyStatus to true
        if (!this.gpsScanInProgress && !this.wifiScanInProgress) {
            setReadyIndicator(true);
        }

        if (this.wifiScanInProgress) {
            setReadyIndicator(false);
        }
    }

    public int getCurrentScanId() {
        return currentScanId;
    }

    /**
     * Exports the contents of a database as csv
     * @return A Pair of Lists of Strings. The first list is wifi observations, the second is
     * gps pings.
     */
    private Pair<List<String>,List<String>> exportDatabaseCSV() {
        WifiSurveyDatabase db = WifiSurveyDatabase.getInstance(this);
        LocationPingDAO lpd = db.getLocationPingDao();
        WifiObservationDAO wod = db.getWifiObservationDao();

        List<String> observations = new ArrayList<String>();
        wod.getWifiObservations().forEach(i->observations.add(i.toCSV()));

        List<String> pings = new ArrayList<String>();
        lpd.getLocationPings().forEach(i->pings.add(i.toCSV()));

        //Save the file and get a URI
        //TODO Generate a random file name
        File file = new File(this.getFilesDir(), "export.csv");
        //TODO actually write the data to file

        try {
            FileWriter writer = new FileWriter(file);
            for (String obs : observations) {
                writer.write(obs);
                writer.write("\n");
            }

            writer.write("\n");

            for (String ping : pings) {
                writer.write(ping);
                writer.write("\n");
            }

            writer.close();
        } catch (Exception e) {
            Log.d("WiFi Survey:exportDatabaseCSV", e.toString());
        }

        //https://developer.android.com/reference/androidx/core/content/FileProvider#ProviderDefinition
        //https://developer.android.com/training/sharing/send#send-binary-content
        Uri uri = FileProvider.getUriForFile(this, "com.example.fileprovider", file);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntent.setType("text/csv");

        startActivity(Intent.createChooser(shareIntent, null));

        return new Pair<>(observations, pings);
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
        LocationPing obs = new LocationPing(t, getCurrentScanId());
        Log.d("WiFi Survey:addLocationPing", "Timestamp nanos: " + obs.getTimeSinceBootNanos());
        WifiSurveyDatabase db = WifiSurveyDatabase.getInstance(this);
        db.getLocationPingDao().insertLocationPing(obs);

        setGpsScanInProgress(false);
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

                    WifiObservation obs = new WifiObservation(i, getCurrentScanId());
                    WifiSurveyDatabase.getInstance(context).getWifiObservationDao().insertWifiObservation(obs);
                }

                setWifiScanInProgress(false);
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

        //Ignore repeated settings. IE setting it true multiple times in a row
        if (status == scanReadyStatus) {
            Log.d("WiFi Survey:setReadyIndicator", "Tried to reset status to same status.");
            return;
        }

        if (status) {
            button.setText(getResources().getString(R.string.ready));
            scanReadyStatus = true;
            currentScanId += 1;
            Log.d("WiFi Survey:setReadyIndicator", "Setting status to ready. New scanId is: " + currentScanId);
        } else {
            button.setText(getResources().getString(R.string.wait));
            scanReadyStatus = false;
            Log.d("WiFi Survey:setReadyIndicator", "Setting status to false.");
        }
    }

    /**
     * Launches the WiFi and GPS scans.
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    public void doScan()
    {
        wifiScanInProgress = true;
        gpsScanInProgress = true;
        setReadyIndicator(false);

        //Perform the scans
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null,
                ContextCompat.getMainExecutor(this), gpsCallback);
        //Run the scan
        wifiManager.startScan();
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