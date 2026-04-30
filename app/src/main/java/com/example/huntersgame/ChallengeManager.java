
/// dont need

package com.example.huntersgame;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChallengeManager {

    private SharedPreferences prefs;
    private Map<String, Challenge> dailyChallenges;
    private String lastResetDate;

    public ChallengeManager(Context context) {
        prefs = context.getSharedPreferences("challenges", Context.MODE_PRIVATE);
        dailyChallenges = new HashMap<>();
        checkDailyReset();
        loadChallenges();
    }

    private void checkDailyReset() {
        String today = getTodayDate();
        lastResetDate = prefs.getString("last_reset_date", "");

        if (!lastResetDate.equals(today)) {
            // Reset daily challenges
            prefs.edit().putString("last_reset_date", today).apply();
            resetChallenges();
        }
    }

    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
    }

    private void resetChallenges() {
        // Clear old challenges
        dailyChallenges.clear();

        // Create new daily challenges
        dailyChallenges.put("catch_3", new Challenge("Catch 3 creatures", 0, 3, 50));
        dailyChallenges.put("perfect_throws", new Challenge("Make perfect throws", 0, 3, 75));
        dailyChallenges.put("total_captures", new Challenge("Capture any creature", 0, 5, 100));

        saveChallenges();
    }

    private void loadChallenges() {
        // Load from SharedPreferences
        int catch3Progress = prefs.getInt("catch_3_progress", 0);
        boolean catch3Claimed = prefs.getBoolean("catch_3_claimed", false);

        int perfectProgress = prefs.getInt("perfect_throws_progress", 0);
        boolean perfectClaimed = prefs.getBoolean("perfect_throws_claimed", false);

        int totalProgress = prefs.getInt("total_captures_progress", 0);
        boolean totalClaimed = prefs.getBoolean("total_captures_claimed", false);

        Challenge catch3 = new Challenge("Catch 3 creatures", catch3Progress, 3, 50);
        catch3.setClaimed(catch3Claimed);
        dailyChallenges.put("catch_3", catch3);

        Challenge perfect = new Challenge("Make perfect throws", perfectProgress, 3, 75);
        perfect.setClaimed(perfectClaimed);
        dailyChallenges.put("perfect_throws", perfect);

        Challenge total = new Challenge("Capture any creature", totalProgress, 5, 100);
        total.setClaimed(totalClaimed);
        dailyChallenges.put("total_captures", total);
    }

    private void saveChallenges() {
        prefs.edit()
                .putInt("catch_3_progress", dailyChallenges.get("catch_3").getProgress())
                .putBoolean("catch_3_claimed", dailyChallenges.get("catch_3").isClaimed())
                .putInt("perfect_throws_progress", dailyChallenges.get("perfect_throws").getProgress())
                .putBoolean("perfect_throws_claimed", dailyChallenges.get("perfect_throws").isClaimed())
                .putInt("total_captures_progress", dailyChallenges.get("total_captures").getProgress())
                .putBoolean("total_captures_claimed", dailyChallenges.get("total_captures").isClaimed())
                .apply();
    }

    public void recordCapture(String creatureName, boolean wasPerfect) {
        // Update catch_3 challenge
        Challenge catch3 = dailyChallenges.get("catch_3");
        if (!catch3.isCompleted() && !catch3.isClaimed()) {
            catch3.incrementProgress();
        }

        // Update perfect throws challenge
        if (wasPerfect) {
            Challenge perfect = dailyChallenges.get("perfect_throws");
            if (!perfect.isCompleted() && !perfect.isClaimed()) {
                perfect.incrementProgress();
            }
        }

        // Update total captures challenge
        Challenge total = dailyChallenges.get("total_captures");
        if (!total.isCompleted() && !total.isClaimed()) {
            total.incrementProgress();
        }

        saveChallenges();
    }

    public int getCompletedChallengeCount() {
        int count = 0;
        for (Challenge c : dailyChallenges.values()) {
            if (c.isCompleted()) count++;
        }
        return count;
    }

    public int claimAllRewards() {
        int totalXP = 0;
        for (Challenge c : dailyChallenges.values()) {
            if (c.isCompleted() && !c.isClaimed()) {
                totalXP += c.getRewardXP();
                c.setClaimed(true);
            }
        }
        saveChallenges();
        return totalXP;
    }

    public Map<String, Challenge> getDailyChallenges() {
        return dailyChallenges;
    }

    public class Challenge {
        private String description;
        private int progress;
        private int target;
        private int rewardXP;
        private boolean claimed;

        public Challenge(String description, int progress, int target, int rewardXP) {
            this.description = description;
            this.progress = progress;
            this.target = target;
            this.rewardXP = rewardXP;
            this.claimed = false;
        }

        public String getDescription() { return description; }
        public int getProgress() { return progress; }
        public int getTarget() { return target; }
        public int getRewardXP() { return rewardXP; }
        public boolean isCompleted() { return progress >= target; }
        public boolean isClaimed() { return claimed; }
        public void setClaimed(boolean claimed) { this.claimed = claimed; }
        public void incrementProgress() { if (progress < target) progress++; }
    }
}