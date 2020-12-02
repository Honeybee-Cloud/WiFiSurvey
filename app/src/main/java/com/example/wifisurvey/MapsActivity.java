package com.example.wifisurvey;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCircleClickListener {

    private GoogleMap mMap;
    private Circle curCircle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Simple helper function to convert nanosecond to microseconds
     * @param nanos a time in nanoseconds
     * @return the argument in microseconds
     */
    private long nanoToMicro(long nanos) {
        return nanos / 1000L;
    }

    /**
     *
     * @param circle
     */
    @Override
    public void onCircleClick(Circle circle) {
        Log.d("WiFi Survey:onCircleClick", "New circle clicked.");

        if (curCircle != null) {
            curCircle.setFillColor(Color.TRANSPARENT);
        }

        curCircle = circle;

        circle.setFillColor(Color.RED);

        CircleTag tag = (CircleTag)circle.getTag();
        if (tag == null) {
            Log.d("WiFi Survey:onCircleClick", "Tag is null.");
            return;
        }
    }

    /**
     * This class is added to the Map marker circles as a tag. When a user clicks a circle we
     * can retrieve the relevant scan data
     */
    private static class CircleTag {
        private List<WifiObservation> observations;
        private LocationPing ping;

        public CircleTag(LocationPing _ping, List<WifiObservation> _observations) {
            ping = _ping;
            observations = _observations;
        }

        public LocationPing getPing() {
            return ping;
        }

        public void setPing(LocationPing ping) {
            this.ping = ping;
        }

        public List<WifiObservation> getObservations() {
            return observations;
        }

        public void setObservations(List<WifiObservation> observations) {
            this.observations = observations;
        }
    }

    /**
     * Clears the map and redraws pings
     */
    private void updateLocationPings() {
        mMap.clear();
        WifiSurveyDatabase db = WifiSurveyDatabase.getInstance(this);

        //Most recent first. We will iterate over it backwards relative to time
        List<LocationPing> pings = db.getLocationPingDao().getLocationsByTimeDesc();

        long next_time_micros = Long.MAX_VALUE;

        for (LocationPing p : pings) {
            /*
            Accuracy: https://developer.android.com/reference/android/location/Location#getAccuracy()
             */

            //WiFi observations have timestamp in micros since boot
            //GPS fixes have it in nanos since boot
            long cur_time_micros = nanoToMicro(p.getTimeSinceBootNanos());

            //Get all WifiObservations that occurred after this location fix and before the next chronological fix
            //TODO find a better and more rigorous way of ordering scans
            List<WifiObservation> observations =
                    db.getWifiObservationDao().getObservationsBetweenTimesAsc(cur_time_micros, next_time_micros);

            Log.d("WiFi Survey:updateLocationPings",
                    "Got observations between (" + cur_time_micros + ", " + next_time_micros + ")");
            Log.d("WiFi Survey:updateLocationPings", "Number of observations: " + observations.size());

            //Get the Lat/Lng for each GPS ping
            LatLng latlng = new LatLng(p.getLatitude(), p.getLongitude());

            mMap.addCircle(new CircleOptions()
                    .center(latlng)
                    .clickable(true)
                    .radius(p.getAccuracy())
                    .strokeColor(Color.RED)
                    .fillColor(Color.TRANSPARENT))
                    .setTag(new CircleTag(p, observations));

            Log.d("WiFi Survey:updateLocationPings", "Moving camera to: " + latlng.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));

            next_time_micros = cur_time_micros;
        }

        //Add any remaining Observations to the last element
        List<WifiObservation> observations =
                db.getWifiObservationDao().getObservationsBetweenTimesAsc(0L, next_time_micros);
        Log.d("WiFi Survey:updateLocationPings", "Size of remainders: " + observations.size());
        for (WifiObservation obs : observations) {
            Log.d("WiFi Survey:updateLocationPings", "Time since boot in micros: " + obs.getTimeSinceBootMicros());
        }
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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        updateLocationPings();

        mMap.setOnCircleClickListener(this);
    }
}