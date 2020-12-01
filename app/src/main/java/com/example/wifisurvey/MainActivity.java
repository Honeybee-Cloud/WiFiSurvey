package com.example.wifisurvey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager = null;
    private String clipboardText = "";
    private LocationManager locationManager = null;
    private ClipboardManager clipboardManager = null;
    private boolean scanReadyStatus = true;

    private void addLocationPing(Location t) {
        Log.d("WiFi Survey:addLocationPing", "Attempting to add Location: " + t.toString());
        LocationPing obs = new LocationPing(t);
        WifiSurveyDatabase db = WifiSurveyDatabase.getInstance(this);
        db.getLocationPingDao().insertLocationPing(obs);

        List<LocationPing> pings = db.getLocationPingDao().getLocationPings();
    }

    private final Consumer<Location> gpsCallback = new Consumer<Location>() {
        @SuppressLint("MissingPermission")
        @Override
        public void accept(Location t)
        {
            Log.d("WiFi Survey:gpsCallback", t.toString().trim());
            addLocationPing(t);
        }
    };

    private final BroadcastReceiver wifiScanCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                for (ScanResult i : mScanResults)
                {
                    Log.d("WiFi Survey:wifiScanCallback", "Adding observation: " + i.toString());
                    setReadyIndicator(true);

                    WifiObservation obs = new WifiObservation(i);
                    WifiSurveyDatabase.getInstance(context).getWifiObservationDao().insertWifiObservation(obs);
                }
            }
        }
    };

    private void setReadyIndicator(boolean status) {
        Button button = (Button)findViewById(R.id.readyIndicator);

        if (status == true) {
            button.setText(getResources().getString(R.string.ready));
            scanReadyStatus = true;
        } else {
            button.setText(getResources().getString(R.string.wait));
            scanReadyStatus = false;
        }
    }

    public void copyToClipboard() {
        ClipData clip = ClipData.newPlainText("", clipboardText);
        clipboardManager.setPrimaryClip(clip);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Check for permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
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
        }

        scanReadyStatus = true;

        //For the copy button
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

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
        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doScan();
            }
        });

        //Callback for Copy
        findViewById(R.id.copyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard();
            }
        });

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}