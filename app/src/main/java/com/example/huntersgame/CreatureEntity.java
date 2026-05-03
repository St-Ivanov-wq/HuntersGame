package com.example.huntersgame;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "creatures")
public class CreatureEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String nameBulgarian;
    private String description;
    private String habitat;
    private int pointsValue;
    private int imageResId;
    private int captureCount;
    private boolean isCaptured;
    private boolean habitatUnlocked;
    private boolean descriptionUnlocked;
    private boolean fullInfoUnlocked;

    public CreatureEntity(@NonNull String id, String name, String nameBulgarian, String description,
                          String habitat, int pointsValue, int imageResId) {
        this.id = id;
        this.name = name;
        this.nameBulgarian = nameBulgarian;
        this.description = description;
        this.habitat = habitat;
        this.pointsValue = pointsValue;
        this.imageResId = imageResId;
        this.captureCount = 0;
        this.isCaptured = false;
        this.habitatUnlocked = false;
        this.descriptionUnlocked = false;
        this.fullInfoUnlocked = false;
    }

    @NonNull
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameBulgarian() { return nameBulgarian; }
    public String getDescription() { return description; }
    public String getHabitat() { return habitat; }
    public int getPointsValue() { return pointsValue; }
    public int getImageResId() { return imageResId; }
    public int getCaptureCount() { return captureCount; }
    public boolean isCaptured() { return isCaptured; }
    public boolean isHabitatUnlocked() { return habitatUnlocked; }
    public boolean isDescriptionUnlocked() { return descriptionUnlocked; }
    public boolean isFullInfoUnlocked() { return fullInfoUnlocked; }

    public void setId(@NonNull String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setNameBulgarian(String nameBulgarian) { this.nameBulgarian = nameBulgarian; }
    public void setDescription(String description) { this.description = description; }
    public void setHabitat(String habitat) { this.habitat = habitat; }
    public void setPointsValue(int pointsValue) { this.pointsValue = pointsValue; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setCaptureCount(int captureCount) {
        this.captureCount = captureCount;
        updateUnlockLevels();
    }
    public void setCaptured(boolean captured) { this.isCaptured = captured; }
    public void setHabitatUnlocked(boolean unlocked) { this.habitatUnlocked = unlocked; }
    public void setDescriptionUnlocked(boolean unlocked) { this.descriptionUnlocked = unlocked; }
    public void setFullInfoUnlocked(boolean unlocked) { this.fullInfoUnlocked = unlocked; }

    private void updateUnlockLevels() {
        if (captureCount >= 10) {
            fullInfoUnlocked = true;
            descriptionUnlocked = true;
            habitatUnlocked = true;
        } else if (captureCount >= 5) {
            descriptionUnlocked = true;
            habitatUnlocked = true;
            fullInfoUnlocked = false;
        } else if (captureCount >= 2) {
            habitatUnlocked = true;
            descriptionUnlocked = false;
            fullInfoUnlocked = false;
        } else {
            habitatUnlocked = false;
            descriptionUnlocked = false;
            fullInfoUnlocked = false;
        }
    }

}