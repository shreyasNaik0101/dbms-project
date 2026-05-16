package com.suraksha.setu.models;

/** Represents a gig platform partner (Swiggy, Uber, etc.). */
public class GigPlatform {
    private int    platformId;
    private String platformName;

    public GigPlatform() {}

    public GigPlatform(int platformId, String platformName) {
        this.platformId   = platformId;
        this.platformName = platformName;
    }

    public int    getPlatformId()               { return platformId; }
    public void   setPlatformId(int id)         { this.platformId = id; }
    public String getPlatformName()             { return platformName; }
    public void   setPlatformName(String name)  { this.platformName = name; }

    @Override
    public String toString() { return platformName; }
}
