package com.example.wifisurvey;

import android.location.Location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Time;

@Entity(tableName = "LocationPings")
public class LocationPing {

    /*
    https://developer.android.com/reference/android/location/Location#getElapsedRealtimeNanos()
    */

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;

    private double longitude;
    private double latitude;
    private float accuracy;

    private double altitude;
    private float verticalAccuracyMeters;

    private float bearing;
    private float bearingAccuracyDegrees;

    private float speed;
    private float speedAccuracyMetersPerSecond;

    public LocationPing () {

    }

    public LocationPing(Location loc) {
        setTimestamp(loc.getElapsedRealtimeNanos());
        setAccuracy(loc.getAccuracy());
        setAltitude(loc.getAltitude());
        setBearing(loc.getBearing());
        setBearingAccuracyDegrees(loc.getBearingAccuracyDegrees());
        setLatitude(loc.getLatitude());
        setLongitude(loc.getLongitude());
        setSpeed(loc.getSpeed());
        setSpeedAccuracyMetersPerSecond(loc.getSpeedAccuracyMetersPerSecond());
        setVerticalAccuracyMeters(loc.getVerticalAccuracyMeters());
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getBearingAccuracyDegrees() {
        return bearingAccuracyDegrees;
    }

    public void setBearingAccuracyDegrees(float bearingAccuracyDegrees) {
        this.bearingAccuracyDegrees = bearingAccuracyDegrees;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeedAccuracyMetersPerSecond() {
        return speedAccuracyMetersPerSecond;
    }

    public void setSpeedAccuracyMetersPerSecond(float speedAccuracyMetersPerSecond) {
        this.speedAccuracyMetersPerSecond = speedAccuracyMetersPerSecond;
    }

    public float getVerticalAccuracyMeters() {
        return verticalAccuracyMeters;
    }

    public void setVerticalAccuracyMeters(float verticalAccuracyMeters) {
        this.verticalAccuracyMeters = verticalAccuracyMeters;
    }
}
