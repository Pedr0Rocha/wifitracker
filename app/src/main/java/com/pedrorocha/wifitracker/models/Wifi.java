package com.pedrorocha.wifitracker.models;

import com.google.firebase.database.Exclude;
import com.pedrorocha.wifitracker.R;
import com.pedrorocha.wifitracker.Utils;

public class Wifi {

    private String scanId;
    private String ssid;
    private String bssid;
    private String capabilities;
    private long timestamp;
    private String parsedTimestamp;
    private boolean isOpen;
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
        this.parsedTimestamp = Utils.getParsedTimestamp(timestamp);
        this.isOpen = hasNoPassword();

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
        return this.parsedTimestamp;
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

    public boolean isOpen() {
        return isOpen;
    }

    private void setupSecurityAttributes() {
        if (isWEP()) {
            this.color = R.color.yellow;
            this.securityLevel = "Weak";
        } else if (isOpen) {
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

    @Override
    public String toString() {
        return "ssid: " + ssid +
               "\nbssid: " + bssid +
               "\ncapabilities: " + capabilities +
               "\ntimestamp: " + parsedTimestamp +
               "\nsecurity level: " + securityLevel +
               "\nopen: " + isOpen;
    }
}
