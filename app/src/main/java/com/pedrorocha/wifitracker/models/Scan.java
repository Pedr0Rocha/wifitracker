package com.pedrorocha.wifitracker.models;

import com.pedrorocha.wifitracker.Utils;

public class Scan {

    private String id;
    private long timestamp;
    private String parsedTimestamp;
    private int wifisFound;

    public Scan() {}

    public Scan(String id, long timestamp, int wifisFound) {
        this.id = id;
        this.timestamp = timestamp;
        this.parsedTimestamp = Utils.getParsedTimestamp(timestamp);
        this.wifisFound = wifisFound;
    }

    public String getId() {
        return id;
    }

    public String getParsedTimestamp() {
        return parsedTimestamp;
    }

    public int getWifisFound() {
        return wifisFound;
    }
}
