package com.suraksha.setu.models;

import java.sql.Date;

/**
 * Represents a single gig work entry (one day of work on one platform).
 * Demonstrates encapsulation, default + parameterized constructors.
 */
public class WorkHistory {
    private int    entryId;
    private int    workerId;
    private int    platformId;
    private String platformName;  // joined field for display
    private Date   workDate;
    private double hoursLogged;
    private double earnings;
    private Date   completionDate;

    // Default constructor
    public WorkHistory() {}

    // Parameterized constructor
    public WorkHistory(int entryId, int workerId, int platformId,
                       Date workDate, double hoursLogged, double earnings, Date completionDate) {
        this.entryId        = entryId;
        this.workerId       = workerId;
        this.platformId     = platformId;
        this.workDate       = workDate;
        this.hoursLogged    = hoursLogged;
        this.earnings       = earnings;
        this.completionDate = completionDate;
    }

    // Getters & Setters
    public int    getEntryId()                         { return entryId; }
    public void   setEntryId(int entryId)              { this.entryId = entryId; }
    public int    getWorkerId()                        { return workerId; }
    public void   setWorkerId(int workerId)            { this.workerId = workerId; }
    public int    getPlatformId()                      { return platformId; }
    public void   setPlatformId(int platformId)        { this.platformId = platformId; }
    public String getPlatformName()                    { return platformName; }
    public void   setPlatformName(String platformName) { this.platformName = platformName; }
    public Date   getWorkDate()                        { return workDate; }
    public void   setWorkDate(Date workDate)           { this.workDate = workDate; }
    public double getHoursLogged()                     { return hoursLogged; }
    public void   setHoursLogged(double hoursLogged)   { this.hoursLogged = hoursLogged; }
    public double getEarnings()                        { return earnings; }
    public void   setEarnings(double earnings)         { this.earnings = earnings; }
    public Date   getCompletionDate()                  { return completionDate; }
    public void   setCompletionDate(Date d)            { this.completionDate = d; }

    @Override
    public String toString() {
        return String.format("WorkHistory{date=%s, platform=%s, hours=%.1f, earnings=%.2f}",
                workDate, platformName, hoursLogged, earnings);
    }
}
