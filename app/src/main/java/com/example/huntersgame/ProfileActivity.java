package com.example.huntersgame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends BaseActivity {

    private DatabaseManager dbManager;
    private LinearLayout profileContainer;
    private Button btnBack;
    private Button btnResetAll;
    private TextView tvUsername;
    private TextView tvProfileLevel;
    private ImageView ivEditUsername;
    private SettingsManager settingsManager;

    private int totalXP = 0;
    private int currentLevel = 1;
    private int xpToNextLevel = 100;
    private int totalCaptures = 0;
    private int discoveredSpecies = 0;
    private int totalSpecies = 14;
    private String topCreature = "";
    private int topCreatureCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileContainer = findViewById(R.id.profile_container);
        btnBack = findViewById(R.id.btn_back_profile);
        btnResetAll = findViewById(R.id.btn_reset_all);
        tvUsername = findViewById(R.id.tv_username);
        tvProfileLevel = findViewById(R.id.tv_profile_level);
        ivEditUsername = findViewById(R.id.iv_edit_username);

        settingsManager = new SettingsManager(this);

        dbManager = DatabaseManager.getInstance(this);

        String username = settingsManager.getUsername();
        tvUsername.setText(username);

        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playButtonClick();
            finish();
        });

        btnResetAll.setOnClickListener(v -> {
            SoundManager.getInstance(this).playButtonClick();
            showResetDialog();
        });


        ivEditUsername.setOnClickListener(v -> {
            SoundManager.getInstance(this).playButtonClick();
            showEditUsernameDialog();
        });

        loadProfileData();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Hunter Name");
        final EditText input = new EditText(this);
        input.setText(settingsManager.getUsername());
        input.setHint("Enter your hunter name");
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                settingsManager.setUsername(newName);
                tvUsername.setText(newName);
                Toast.makeText(this, "Name changed to " + newName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
                updateAchievements();
                displayProfile();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileActivity", "Error loading data: " + error);
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAchievements() {
        AchievementManager am = AchievementManager.getInstance(this);
        am.checkFirstCatch(totalCaptures);
        am.checkNoviceHunter(totalCaptures);
        am.checkCollector(discoveredSpecies);
        am.checkLevelUp(currentLevel);
        am.checkUltimateCollector(totalCaptures);
        am.checkMasterHunter(discoveredSpecies, totalSpecies);
    }
    private void calculateStats(List<CreatureEntity> creatures) {
        totalCaptures = 0;
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

        dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer xp) {
                totalXP = xp;
                currentLevel = 1 + (totalXP / 100);
                xpToNextLevel = 100 - (totalXP % 100);
                updateLevelDisplay();
                updateAchievements();
                displayProfile();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileActivity", "Error getting XP: " + error);
            }
        });
    }

    private void updateLevelDisplay() {
        if (tvProfileLevel != null) {
            tvProfileLevel.setText("LEVEL " + currentLevel + " • " + totalXP + " XP");
        }
    }
    private void displayProfile() {
        profileContainer.removeAllViews();
        addHeaderCard();
        addStatsCard();
        addProgressCard();
        addAchievementsCard();
    }

    private void addHeaderCard() {
        CardView card = createCard();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(40, 40, 40, 40);
        content.setGravity(Gravity.CENTER);

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

        TextView title = new TextView(this);
        title.setText("HUNTER STATS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, 16);

        addStatItem(row1, "🎯", "Total Captures", String.valueOf(totalCaptures));
        addStatItem(row1, "📖", "Bestiary", discoveredSpecies + "/" + totalSpecies);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);

        addStatItem(row2, "⭐", "Total XP", String.valueOf(totalXP));
        addStatItem(row2, "🏆", "Favorite", topCreature.isEmpty() ? "None" : topCreature);

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);

        addStatItem(row3, "📅", "Date", new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date()));
        addStatItem(row3, "🎮", "Version", "1.0.0");

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

        TextView title = new TextView(this);
        title.setText("PROGRESS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        TextView xpLabel = new TextView(this);
        xpLabel.setText("Next Level");
        xpLabel.setTextSize(12);
        xpLabel.setTextColor(Color.parseColor("#AAAAAA"));
        content.addView(xpLabel);

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

        TextView title = new TextView(this);
        title.setText("ACHIEVEMENTS");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        AchievementManager am = AchievementManager.getInstance(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, 16);
        addAchievementItem(row1, "🎯", "First Catch", am.hasFirstCatch(), 0, am);
        addAchievementItem(row1, "⭐", "Novice Hunter", am.hasNoviceHunter(), 1, am);
        grid.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);
        addAchievementItem(row2, "📖", "Collector", am.hasCollector(), 2, am);
        addAchievementItem(row2, "✨", "Perfect Start", am.hasPerfectStart(), 3, am);
        grid.addView(row2);

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.setPadding(0, 0, 0, 16);
        addAchievementItem(row3, "🏆", "Level 5", am.hasLevel5(), 4, am);
        addAchievementItem(row3, "⚡", "Combo Novice", am.hasComboNovice(), 5, am);
        grid.addView(row3);

        LinearLayout row4 = new LinearLayout(this);
        row4.setOrientation(LinearLayout.HORIZONTAL);
        row4.setPadding(0, 0, 0, 16);
        addAchievementItem(row4, "🐉", "Species Master", am.hasSpeciesMaster(), 6, am);
        addAchievementItem(row4, "👑", "Level 10", am.hasLevel10(), 7, am);
        grid.addView(row4);

        LinearLayout row5 = new LinearLayout(this);
        row5.setOrientation(LinearLayout.HORIZONTAL);
        row5.setPadding(0, 0, 0, 16);
        addAchievementItem(row5, "🎯", "Perfect Master", am.hasPerfectMaster(), 8, am);
        addAchievementItem(row5, "🔥", "Combo Expert", am.hasComboExpert(), 9, am);
        grid.addView(row5);

        LinearLayout row6 = new LinearLayout(this);
        row6.setOrientation(LinearLayout.HORIZONTAL);
        row6.setPadding(0, 0, 0, 16);
        addAchievementItem(row6, "🌟", "Master Hunter", am.hasMasterHunter(), 10, am);
        addAchievementItem(row6, "💥", "Combo Legend", am.hasComboLegend(), 11, am);
        grid.addView(row6);

        LinearLayout row7 = new LinearLayout(this);
        row7.setOrientation(LinearLayout.HORIZONTAL);
        addAchievementItem(row7, "🏅", "Ultimate Collector", am.hasUltimateCollector(), 12, am);
        addAchievementItem(row7, "🎯", "Perfectionist", am.hasPerfectionist(), 13, am);
        grid.addView(row7);

        LinearLayout row8 = new LinearLayout(this);
        row8.setOrientation(LinearLayout.HORIZONTAL);
        row8.setGravity(Gravity.CENTER);
        addAchievementItem(row8, "🏆", "Mythical Legend", am.hasLevel25(), 14, am);
        grid.addView(row8);

        scrollView.addView(grid);
        content.addView(scrollView);
        card.addView(content);
        profileContainer.addView(card);
    }

    private void addAchievementItem(LinearLayout parent, String icon, String name, boolean unlocked, int index, AchievementManager am) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        item.setLayoutParams(params);
        item.setClickable(true);
        item.setFocusable(true);

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

        final int achievementIndex = index;
        item.setOnClickListener(v -> {
            SoundManager.getInstance(this).playButtonClick();
            am.showAchievementInfo(achievementIndex);
        });

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
                AchievementManager am = AchievementManager.getInstance(ProfileActivity.this);
                currentLevel = 1;
                am.resetAllAchievements();
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