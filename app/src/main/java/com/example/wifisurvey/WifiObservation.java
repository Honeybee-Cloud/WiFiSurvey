package com.example.wifisurvey;

import android.net.wifi.ScanResult;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="WifiObservations")
public class WifiObservation {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String bssid;
    private String ssid;
    private String capabilities;
    private int centerFreq0;
    private int centerFreq1;
    private int channelWidth;
    private int frequency;
    private int level;
    private long timeSinceBootMicros;

    public WifiObservation() {

    }

    public WifiObservation(ScanResult res) {
        setBssid(res.BSSID);
        setSsid(res.SSID);
        setCapabilities(res.capabilities);
        setCenterFreq0(res.centerFreq0);
        setCenterFreq1(res.centerFreq1);
        setFrequency(res.frequency);
        setTimeSinceBootMicros(res.timestamp);
        setLevel(res.level);
        setChannelWidth(res.channelWidth);
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getCenterFreq0() {
        return centerFreq0;
    }

    public void setCenterFreq0(int centerFreq0) {
        this.centerFreq0 = centerFreq0;
    }

    public int getCenterFreq1() {
        return centerFreq1;
    }

    public void setCenterFreq1(int centerFreq1) {
        this.centerFreq1 = centerFreq1;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getChannelWidth() {
        return channelWidth;
    }

    public void setChannelWidth(int channelWidth) {
        this.channelWidth = channelWidth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTimeSinceBootMicros() {
        return timeSinceBootMicros;
    }

    public void setTimeSinceBootMicros(long timeSinceBootMicros) {
        this.timeSinceBootMicros = timeSinceBootMicros;
    }

    public String toCSV() {
        return new StringBuilder()
                .append(getBssid()).append(",")
                .append(getSsid()).append(",")
                .append(getCapabilities()).append(",")
                .append(getCenterFreq0()).append(",")
                .append(getCenterFreq1()).append(",")
                .append(getChannelWidth()).append(",")
                .append(getFrequency()).append(",")
                .append(getLevel()).append(",")
                .append(getTimeSinceBootMicros())
                .toString();
    }
}
