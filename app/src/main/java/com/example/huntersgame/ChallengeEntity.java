package com.example.huntersgame;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "challenges")
public class ChallengeEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String description;
    private String type;
    private int target;
    private int progress;
    private int rewardXP;
    private boolean isCompleted;
    private boolean isClaimed;
    private String lastResetDate;

    public ChallengeEntity(@NonNull String id, String name, String description, String type, int target, int rewardXP) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target;
        this.rewardXP = rewardXP;
        this.progress = 0;
        this.isCompleted = false;
        this.isClaimed = false;
        this.lastResetDate = "";
    }

    @NonNull
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public int getRewardXP() { return rewardXP; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isClaimed() { return isClaimed; }
    public String getLastResetDate() { return lastResetDate; }

    public void setId(@NonNull String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setTarget(int target) { this.target = target; }
    public void setProgress(int progress) {
        this.progress = progress;
        this.isCompleted = progress >= target;
    }
    public void setRewardXP(int rewardXP) { this.rewardXP = rewardXP; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
    public void setClaimed(boolean claimed) { this.isClaimed = claimed; }
    public void setLastResetDate(String lastResetDate) { this.lastResetDate = lastResetDate; }

    public void incrementProgress() {
        if (progress < target) {
            progress++;
            isCompleted = progress >= target;
        }
    }

}