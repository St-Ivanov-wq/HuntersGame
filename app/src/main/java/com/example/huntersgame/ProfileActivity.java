package com.example.huntersgame;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private LinearLayout profileContainer;
    private Button btnBack;
    private Button btnResetAll;

    // Stats
    private int totalXP = 0;
    private int currentLevel = 1;
    private int xpToNextLevel = 100;
    private int totalCaptures = 0;
    private int perfectThrows = 0;
    private int discoveredSpecies = 0;
    private int totalSpecies = 5;
    private String topCreature = "";
    private int topCreatureCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileContainer = findViewById(R.id.profile_container);
        btnBack = findViewById(R.id.btn_back_profile);
        btnResetAll = findViewById(R.id.btn_reset_all);

        dbManager = new DatabaseManager(this);

        btnBack.setOnClickListener(v -> finish());

        btnResetAll.setOnClickListener(v -> showResetDialog());

        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        dbManager.getAllCreatures(new DatabaseManager.Callback<List<CreatureEntity>>() {
            @Override
            public void onSuccess(List<CreatureEntity> creatures) {
                calculateStats(creatures);
                displayProfile();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileActivity", "Error loading data: " + error);
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateStats(List<CreatureEntity> creatures) {
        totalCaptures = 0;
        perfectThrows = 0;
        discoveredSpecies = 0;
        topCreatureCount = 0;
        topCreature = "";

        for (CreatureEntity creature : creatures) {
            int count = creature.getCaptureCount();
            totalCaptures += count;

            if (count > 0) {
                discoveredSpecies++;
            }

            if (count > topCreatureCount) {
                topCreatureCount = count;
                topCreature = creature.getName();
            }
        }

        // Get total XP
        dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer xp) {
                totalXP = xp;
                currentLevel = 1 + (totalXP / 100);
                xpToNextLevel = 100 - (totalXP % 100);
                displayProfile();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileActivity", "Error getting XP: " + error);
            }
        });
    }

    private void displayProfile() {
        profileContainer.removeAllViews();

        // Header Card
        addHeaderCard();

        // Stats Card
        addStatsCard();

        // Progress Card
        addProgressCard();

        // Achievements Card
        addAchievementsCard();

        // Reset Button already exists in layout
    }

    private void addHeaderCard() {
        CardView card = createCard();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(40, 40, 40, 40);
        content.setGravity(Gravity.CENTER);

        // Hunter Icon and Level
        TextView levelText = new TextView(this);
        levelText.setText("🏹");
        levelText.setTextSize(48);
        levelText.setGravity(Gravity.CENTER);

        TextView rankText = new TextView(this);
        String rank = getHunterRank(currentLevel);
        rankText.setText(rank);
        rankText.setTextSize(20);
        rankText.setTypeface(null, Typeface.BOLD);
        rankText.setTextColor(getRankColor(currentLevel));
        rankText.setGravity(Gravity.CENTER);
        rankText.setPadding(0, 8, 0, 0);

        TextView levelInfo = new TextView(this);
        levelInfo.setText("Level " + currentLevel);
        levelInfo.setTextSize(16);
        levelInfo.setTextColor(Color.parseColor("#CCCCCC"));
        levelInfo.setGravity(Gravity.CENTER);
        levelInfo.setPadding(0, 4, 0, 0);

        content.addView(levelText);
        content.addView(rankText);
        content.addView(levelInfo);

        card.addView(content);
        profileContainer.addView(card);
    }

    private void addStatsCard() {
        CardView card = createCard();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(40, 30, 40, 30);

        // Title
        TextView title = new TextView(this);
        title.setText("HUNTER STATS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        // Stats Grid
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        // Row 1
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, 16);

        addStatItem(row1, "🎯", "Total Captures", String.valueOf(totalCaptures));
        addStatItem(row1, "✨", "Perfect Throws", String.valueOf(perfectThrows));

        // Row 2
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);

        addStatItem(row2, "📖", "Bestiary", discoveredSpecies + "/" + totalSpecies);
        addStatItem(row2, "⭐", "Total XP", String.valueOf(totalXP));

        // Row 3
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);

        addStatItem(row3, "🏆", "Favorite", topCreature.isEmpty() ? "None" : topCreature);
        addStatItem(row3, "📅", "Date", new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date()));

        grid.addView(row1);
        grid.addView(row2);
        grid.addView(row3);

        content.addView(grid);
        card.addView(content);
        profileContainer.addView(card);
    }

    private void addStatItem(LinearLayout parent, String icon, String label, String value) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        item.setLayoutParams(params);

        TextView iconText = new TextView(this);
        iconText.setText(icon);
        iconText.setTextSize(24);
        iconText.setGravity(Gravity.CENTER);

        TextView valueText = new TextView(this);
        valueText.setText(value);
        valueText.setTextSize(18);
        valueText.setTypeface(null, Typeface.BOLD);
        valueText.setTextColor(Color.parseColor("#FFD700"));
        valueText.setGravity(Gravity.CENTER);
        valueText.setPadding(0, 4, 0, 4);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextSize(11);
        labelText.setTextColor(Color.parseColor("#888888"));
        labelText.setGravity(Gravity.CENTER);

        item.addView(iconText);
        item.addView(valueText);
        item.addView(labelText);

        parent.addView(item);
    }

    private void addProgressCard() {
        CardView card = createCard();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(40, 30, 40, 30);

        // Title
        TextView title = new TextView(this);
        title.setText("PROGRESS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        // XP Progress
        TextView xpLabel = new TextView(this);
        xpLabel.setText("Next Level");
        xpLabel.setTextSize(12);
        xpLabel.setTextColor(Color.parseColor("#AAAAAA"));
        content.addView(xpLabel);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20));
        progressBar.setMax(100);
        int currentXPInLevel = totalXP % 100;
        progressBar.setProgress(currentXPInLevel);
        progressBar.setProgressTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        content.addView(progressBar);

        LinearLayout xpRow = new LinearLayout(this);
        xpRow.setOrientation(LinearLayout.HORIZONTAL);
        xpRow.setPadding(0, 8, 0, 20);

        TextView currentXpText = new TextView(this);
        currentXpText.setText(currentXPInLevel + " XP");
        currentXpText.setTextSize(11);
        currentXpText.setTextColor(Color.parseColor("#AAAAAA"));

        TextView neededXpText = new TextView(this);
        neededXpText.setText(xpToNextLevel + " XP needed");
        neededXpText.setTextSize(11);
        neededXpText.setTextColor(Color.parseColor("#AAAAAA"));
        neededXpText.setGravity(Gravity.END);

        LinearLayout.LayoutParams xpParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        currentXpText.setLayoutParams(xpParams);
        neededXpText.setLayoutParams(xpParams);

        xpRow.addView(currentXpText);
        xpRow.addView(neededXpText);
        content.addView(xpRow);

        // Bestiary Progress
        TextView bestiaryLabel = new TextView(this);
        bestiaryLabel.setText("Bestiary Completion");
        bestiaryLabel.setTextSize(12);
        bestiaryLabel.setTextColor(Color.parseColor("#AAAAAA"));
        content.addView(bestiaryLabel);

        ProgressBar bestiaryBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bestiaryBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20));
        bestiaryBar.setMax(totalSpecies);
        bestiaryBar.setProgress(discoveredSpecies);
        bestiaryBar.setProgressTintList(ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark));
        content.addView(bestiaryBar);

        TextView bestiaryPercent = new TextView(this);
        int percent = (discoveredSpecies * 100) / totalSpecies;
        bestiaryPercent.setText(percent + "% Complete (" + discoveredSpecies + "/" + totalSpecies + ")");
        bestiaryPercent.setTextSize(11);
        bestiaryPercent.setTextColor(Color.parseColor("#AAAAAA"));
        bestiaryPercent.setPadding(0, 8, 0, 0);
        content.addView(bestiaryPercent);

        card.addView(content);
        profileContainer.addView(card);
    }

    private void addAchievementsCard() {
        CardView card = createCard();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(40, 30, 40, 30);

        // Title
        TextView title = new TextView(this);
        title.setText("ACHIEVEMENTS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        // Achievement Grid
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        // Row 1
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, 16);

        addAchievementItem(row1, "🎯", "First Catch", totalCaptures > 0);
        addAchievementItem(row1, "⭐", "Level 5", currentLevel >= 5);

        // Row 2
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);

        addAchievementItem(row2, "📖", "Collector", discoveredSpecies >= 3);
        addAchievementItem(row2, "✨", "Perfect Master", perfectThrows >= 10);

        // Row 3
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);

        addAchievementItem(row3, "🏆", "Legend", currentLevel >= 10);
        addAchievementItem(row3, "🔍", "Master Hunter", discoveredSpecies == totalSpecies);

        grid.addView(row1);
        grid.addView(row2);
        grid.addView(row3);

        content.addView(grid);
        card.addView(content);
        profileContainer.addView(card);
    }

    private void addAchievementItem(LinearLayout parent, String icon, String name, boolean unlocked) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        item.setLayoutParams(params);

        TextView iconText = new TextView(this);
        iconText.setText(icon);
        iconText.setTextSize(28);
        iconText.setGravity(Gravity.CENTER);
        if (!unlocked) {
            iconText.setAlpha(0.3f);
        }

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(11);
        nameText.setTextColor(unlocked ? Color.parseColor("#FFD700") : Color.parseColor("#444444"));
        nameText.setGravity(Gravity.CENTER);
        nameText.setPadding(0, 4, 0, 0);

        TextView statusText = new TextView(this);
        statusText.setText(unlocked ? "✓" : "🔒");
        statusText.setTextSize(10);
        statusText.setTextColor(unlocked ? Color.parseColor("#4CAF50") : Color.parseColor("#666666"));
        statusText.setGravity(Gravity.CENTER);

        item.addView(iconText);
        item.addView(nameText);
        item.addView(statusText);

        parent.addView(item);
    }

    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset All Progress")
                .setMessage("This will reset ALL your progress including captures, XP, and levels. This cannot be undone!")
                .setPositiveButton("Reset", (dialog, which) -> resetAllProgress())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetAllProgress() {
        dbManager.resetAllProgress(new DatabaseManager.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(ProfileActivity.this, "All progress has been reset!", Toast.LENGTH_LONG).show();
                loadProfileData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Error resetting: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getHunterRank(int level) {
        if (level >= 50) return "★ LEGENDARY HUNTER ★";
        if (level >= 40) return "✦ MYTHIC HUNTER ✦";
        if (level >= 30) return "MASTER HUNTER";
        if (level >= 20) return "EXPERT HUNTER";
        if (level >= 10) return "SKILLED HUNTER";
        if (level >= 5) return "APPRENTICE HUNTER";
        return "NOVICE HUNTER";
    }

    private int getRankColor(int level) {
        if (level >= 50) return Color.parseColor("#FFD700");
        if (level >= 30) return Color.parseColor("#C0C0C0");
        if (level >= 20) return Color.parseColor("#CD7F32");
        return Color.parseColor("#FFFFFF");
    }

    private CardView createCard() {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(20, 12, 20, 12);
        card.setLayoutParams(cardParams);
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setCardBackgroundColor(Color.parseColor("#2D2D44"));
        return card;
    }
}