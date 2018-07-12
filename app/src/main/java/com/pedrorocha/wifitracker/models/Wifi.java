package com.pedrorocha.wifitracker.models;

import android.annotation.SuppressLint;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Wifi {

    private String ssid;
    private String bssid;
    private String capabilities;
    private long timestamp;
    private String mode;
    private int channel;
    private int rate;
    private int signal;
    private String security;
    private Location location;

    public Wifi() {

    }

    public Wifi(String ssid, String bssid, String capabilities, long timestamp) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.capabilities = capabilities;
        this.timestamp = timestamp;
    }

    public Wifi(String ssid, String mode, int channel, int rate, int signal, String security, Location location) {
        this.ssid = ssid;
        this.mode = mode;
        this.channel = channel;
        this.rate = rate;
        this.signal = signal;
        this.security = security;
        this.location = location;
    }

    public String getSsid() {
        return ssid;
    }

    public String getMode() {
        return mode;
    }

    public int getChannel() {
        return channel;
    }

    public int getRate() {
        return rate;
    }

    public int getSignal() {
        return signal;
    }

    public String getSecurity() {
        return security;
    }

    public Location getLocation() {
        return location;
    }

    public String getBssid() {
        return bssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getParsedTimestamp() {
        Timestamp stamp = new Timestamp(timestamp);
        Date date = new Date(stamp.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a");
        return sdf.format(date);
    }

    public boolean isWEP() {
        return capabilities.toLowerCase().contains("wep");
    }

    public boolean hasNoPassword() {
        return (!capabilities.toLowerCase().contains("wep") &&
                !capabilities.toLowerCase().contains("wpa") &&
                !capabilities.toLowerCase().contains("eap") &&
                !capabilities.toLowerCase().contains("psk"));
    }
}
