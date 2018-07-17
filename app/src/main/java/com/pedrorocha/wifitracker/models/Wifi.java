package com.pedrorocha.wifitracker.models;

import android.annotation.SuppressLint;

import com.google.firebase.database.Exclude;
import com.pedrorocha.wifitracker.R;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Wifi {

    private String scanId;
    private String ssid;
    private String bssid;
    private String capabilities;
    private long timestamp;
    private String mode;
    private int channel;
    private int rate;
    private int signal;
    private Location location;

    private int color;
    private String securityLevel;

    public Wifi() {

    }

    public Wifi(String scanId, String ssid, String bssid, String capabilities, long timestamp) {
        this.scanId = scanId;
        this.ssid = ssid;
        this.bssid = bssid;
        this.capabilities = capabilities;
        this.timestamp = timestamp;

        setupSecurityAttributes();
    }

    public String getSsid() {
        return ssid;
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

    @Exclude
    public boolean isWEP() {
        return capabilities.toLowerCase().contains("wep");
    }

    public boolean hasNoPassword() {
        return (!capabilities.toLowerCase().contains("wep") &&
                !capabilities.toLowerCase().contains("wpa") &&
                !capabilities.toLowerCase().contains("eap") &&
                !capabilities.toLowerCase().contains("psk"));
    }

    private void setupSecurityAttributes() {
        if (isWEP()) {
            this.color = R.color.yellow;
            this.securityLevel = "Weak";
        } else if (hasNoPassword()) {
            this.color = R.color.red;
            this.securityLevel = "None";
        } else {
            this.color = R.color.colorAccent;
            this.securityLevel = "Strong";
        }
    }

    @Exclude
    public int getColor() {
        return color;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public String getScanId() {
        return scanId;
    }
}
