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
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameBulgarian() { return nameBulgarian; }
    public String getDescription() { return description; }
    public String getHabitat() { return habitat; }
    public int getPointsValue() { return pointsValue; }
    public int getImageResId() { return imageResId; }
    public boolean isCaptured() { return isCaptured; }
    public void setCaptured(boolean captured) { isCaptured = captured; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}