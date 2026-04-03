package com.example.huntersgame;

public class Creature {
    private String id;
    private String name;
    private String nameBulgarian;
    private String description;
    private String habitat;
    private int pointsValue;
    private int imageResId;
    private boolean isCaptured;
    private int captureCount;
    private double latitude;
    private double longitude;

    // Constructor
    public Creature(String id, String name, String nameBulgarian, String description,
                    String habitat, int pointsValue, int imageResId) {
        this.id = id;
        this.name = name;
        this.nameBulgarian = nameBulgarian;
        this.description = description;
        this.habitat = habitat;
        this.pointsValue = pointsValue;
        this.imageResId = imageResId;
        this.isCaptured = false;
        this.captureCount = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameBulgarian() { return nameBulgarian; }
    public String getDescription() { return description; }
    public String getHabitat() { return habitat; }
    public int getPointsValue() { return pointsValue; }
    public int getImageResId() { return imageResId; }
    public boolean isCaptured() { return isCaptured; }
    public int getCaptureCount() { return captureCount; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setCaptured(boolean captured) {
        this.isCaptured = captured;
    }
    public void setCaptureCount(int count) {
        this.captureCount = count;
    }
    public void incrementCaptureCount() {
        this.captureCount++;
        if (this.captureCount > 0) {
            this.isCaptured = true;
        }
    }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}