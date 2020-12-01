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
    private ParcelFileDescriptor fileDescriptor = null;
    private FileOutputStream file = null;

    private static final int CREATE_FILE = 1;
    private Uri saveFile;

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "wifi-survey.csv");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "");

        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle the result of the create file intent
        if (requestCode == CREATE_FILE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    //Returned data should be a URI
                    if (data != null && data.getData() != null) {
                        //Try to open the file
                        try {
                            fileDescriptor = getContentResolver().openFileDescriptor(((Uri) data.getData()), "w");
                            saveFile = ((Uri)data.getData());
                            file = new FileOutputStream(fileDescriptor.getFileDescriptor());
                        } catch (Exception e) {
                            Log.e("WiFi Survey", e.toString());
                        }

                        Log.d("WiFi Survey", "Successfully opened file: " + saveFile.toString());
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private final Consumer<Location> gpsCallback = new Consumer<Location>() {
        @SuppressLint("MissingPermission")
        @Override
        public void accept(Location t)
        {
            Log.d("WiFi Survey", t.toString().trim());
            writeMessage(t.toString().trim());
        }
    };

    private final BroadcastReceiver wifiScanCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                for (ScanResult i : mScanResults)
                {
                    Log.d("WiFi Survey", i.toString().trim());
                    writeMessage(i.toString().trim());

                    setReadyIndicator(true);
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

    private String parseLocation(String raw) {
        /*
        Location[gps 29.790835,-95.406828 hAcc=16 et=+1d3h40m25s166ms alt=1.09197998046875 vel=0.0 vAcc=48 sAcc=2 bAcc=??? {Bundle[mParcelledData.dataSize=96]}]
         */

        return raw.substring(9);
    }

    private String parseWifi(String raw) {
        /*
        SSID: VIZIOCastDisplay8954, BSSID: a6:6a:44:b1:b3:77, capabilities: [ESS], level: -87, frequency: 2462, timestamp: 99622101542, distance: ?(cm), distanceSd: ?(cm), passpoint: no, ChannelBandwidth: 0, centerFreq0: 2462, centerFreq1: 0, standard: 11n, 80211mcResponder: is not supported, Radio Chain Infos: [RadioChainInfo: id=0, level=-87]
         */

        
    }

    private void writeMessage(String msg) {
        //Append timestamp

        msg = msg + "\n";
        clipboardText += msg;
        ((TextView)findViewById(R.id.outputTextBox)).setText(clipboardText);
        try {
            file.write(msg.getBytes());
        } catch (Exception e) {
            Log.e("WiFi Survey", e.toString());
            Log.d("WiFi Survey", msg);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    public void doScan()
    {
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

        //Register GPS callback
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null,
                ContextCompat.getMainExecutor(this), gpsCallback);

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

        //Callback for New
        findViewById(R.id.newButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}