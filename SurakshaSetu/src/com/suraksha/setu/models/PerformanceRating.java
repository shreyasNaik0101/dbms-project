package com.suraksha.setu.models;

/**
 * Represents aggregated performance rating for a worker on a platform.
 */
public class PerformanceRating {
    private int    ratingId;
    private int    workerId;
    private int    platformId;
    private String platformName;
    private double avgRating;       // 0.0 – 5.0
    private int    totalCompleted;

    public PerformanceRating() {}

    public PerformanceRating(int ratingId, int workerId, int platformId,
                             double avgRating, int totalCompleted) {
        this.ratingId       = ratingId;
        this.workerId       = workerId;
        this.platformId     = platformId;
        this.avgRating      = avgRating;
        this.totalCompleted = totalCompleted;
    }

    public int    getRatingId()                        { return ratingId; }
    public void   setRatingId(int ratingId)            { this.ratingId = ratingId; }
    public int    getWorkerId()                        { return workerId; }
    public void   setWorkerId(int workerId)            { this.workerId = workerId; }
    public int    getPlatformId()                      { return platformId; }
    public void   setPlatformId(int platformId)        { this.platformId = platformId; }
    public String getPlatformName()                    { return platformName; }
    public void   setPlatformName(String platformName) { this.platformName = platformName; }
    public double getAvgRating()                       { return avgRating; }
    public void   setAvgRating(double avgRating)       { this.avgRating = avgRating; }
    public int    getTotalCompleted()                  { return totalCompleted; }
    public void   setTotalCompleted(int totalCompleted){ this.totalCompleted = totalCompleted; }

    @Override
    public String toString() {
        return String.format("Rating{platform=%s, avg=%.2f, completed=%d}",
                platformName, avgRating, totalCompleted);
    }
}
