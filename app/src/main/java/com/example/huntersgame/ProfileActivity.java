package com.example.huntersgame;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private LinearLayout profileContainer;
    private Button btnBack;
    private Button btnResetAll;
   // private Button btnSettings;
    private TextView tvUsername;
    private SettingsManager settingsManager;

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

    // Achievement tracking
    private SharedPreferences achievementPrefs;
    private boolean hadFirstCatch = false;
    private boolean hadLevel5 = false;
    private boolean hadCollector = false;
    private boolean hadPerfectMaster = false;
    private boolean hadLegend = false;
    private boolean hadMasterHunter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileContainer = findViewById(R.id.profile_container);
        btnBack = findViewById(R.id.btn_back_profile);
        btnResetAll = findViewById(R.id.btn_reset_all);
        //btnSettings = findViewById(R.id.btn_settings);
        tvUsername = findViewById(R.id.tv_username);
        ImageView ivEditUsername = findViewById(R.id.iv_edit_username);

        settingsManager = new SettingsManager(this);
        achievementPrefs = getSharedPreferences("achievements", MODE_PRIVATE);

        dbManager = new DatabaseManager(this);

        // Load username
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

        //btnSettings.setOnClickListener(v -> {
           // SoundManager.getInstance(this).playButtonClick();
            //showSettingsDialog();
       // });

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

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);

        SeekBar musicSeekBar = dialogView.findViewById(R.id.music_volume_seekbar);
        SeekBar sfxSeekBar = dialogView.findViewById(R.id.sfx_volume_seekbar);
        Switch vibrationSwitch = dialogView.findViewById(R.id.vibration_switch);
        TextView musicPercent = dialogView.findViewById(R.id.music_volume_percent);
        TextView sfxPercent = dialogView.findViewById(R.id.sfx_volume_percent);

        // Load current values
        float musicVol = settingsManager.getMusicVolume();
        float sfxVol = settingsManager.getSfxVolume();
        musicSeekBar.setProgress((int)(musicVol * 100));
        sfxSeekBar.setProgress((int)(sfxVol * 100));
        musicPercent.setText((int)(musicVol * 100) + "%");
        sfxPercent.setText((int)(sfxVol * 100) + "%");
        vibrationSwitch.setChecked(settingsManager.isVibrationEnabled());

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float vol = progress / 100f;
                settingsManager.setMusicVolume(vol);
                musicPercent.setText(progress + "%");
                SoundManager.getInstance(ProfileActivity.this).setMusicVolume(vol);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sfxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float vol = progress / 100f;
                settingsManager.setSfxVolume(vol);
                sfxPercent.setText(progress + "%");
                SoundManager.getInstance(ProfileActivity.this).setSfxVolume(vol);
                // Test sound
                SoundManager.getInstance(ProfileActivity.this).playButtonClick();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setVibrationEnabled(isChecked);
            if (isChecked) {
                vibrate(50);
            }
        });

        builder.setView(dialogView);
        builder.setTitle("Settings");
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void vibrate(long milliseconds) {
        if (settingsManager.isVibrationEnabled()) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(milliseconds);
            }
        }
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
                loadAchievementStates();
                checkAchievements();
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

    private void loadAchievementStates() {
        hadFirstCatch = achievementPrefs.getBoolean("first_catch", false);
        hadLevel5 = achievementPrefs.getBoolean("level_5", false);
        hadCollector = achievementPrefs.getBoolean("collector", false);
        hadPerfectMaster = achievementPrefs.getBoolean("perfect_master", false);
        hadLegend = achievementPrefs.getBoolean("legend", false);
        hadMasterHunter = achievementPrefs.getBoolean("master_hunter", false);
    }

    private void saveAchievementStates() {
        achievementPrefs.edit()
                .putBoolean("first_catch", hadFirstCatch)
                .putBoolean("level_5", hadLevel5)
                .putBoolean("collector", hadCollector)
                .putBoolean("perfect_master", hadPerfectMaster)
                .putBoolean("legend", hadLegend)
                .putBoolean("master_hunter", hadMasterHunter)
                .apply();
    }

    private void checkAchievements() {
        boolean firstCatch = totalCaptures > 0;
        if (firstCatch && !hadFirstCatch) {
            hadFirstCatch = true;
            showAchievementPopup("First Catch!", "You caught your first creature!");
        }

        boolean level5 = currentLevel >= 5;
        if (level5 && !hadLevel5) {
            hadLevel5 = true;
            showAchievementPopup("Level 5 Hunter!", "Reached Hunter Level 5");
        }

        boolean collector = discoveredSpecies >= 3;
        if (collector && !hadCollector) {
            hadCollector = true;
            showAchievementPopup("Collector!", "Discovered 3 different species");
        }

        boolean perfectMaster = perfectThrows >= 10;
        if (perfectMaster && !hadPerfectMaster) {
            hadPerfectMaster = true;
            showAchievementPopup("Perfect Master!", "Made 10 perfect throws");
        }

        boolean legend = currentLevel >= 10;
        if (legend && !hadLegend) {
            hadLegend = true;
            showAchievementPopup("Legendary Hunter!", "Reached Hunter Level 10");
        }

        boolean masterHunter = discoveredSpecies == totalSpecies;
        if (masterHunter && !hadMasterHunter) {
            hadMasterHunter = true;
            showAchievementPopup("Master Hunter!", "Discovered all species");
        }

        saveAchievementStates();
    }

    private void showAchievementPopup(String title, String description) {
        SoundManager.getInstance(this).playAchievement();
        vibrate(100);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_achievement, null);
        TextView tvTitle = dialogView.findViewById(R.id.achievement_title);
        TextView tvDesc = dialogView.findViewById(R.id.achievement_desc);
        tvTitle.setText(title);
        tvDesc.setText(description);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();

        new Handler().postDelayed(dialog::dismiss, 2000);
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
        addStatItem(row1, "✨", "Perfect Throws", String.valueOf(perfectThrows));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);

        addStatItem(row2, "📖", "Bestiary", discoveredSpecies + "/" + totalSpecies);
        addStatItem(row2, "⭐", "Total XP", String.valueOf(totalXP));

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

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, 16);

        addAchievementItem(row1, "🎯", "First Catch", totalCaptures > 0);
        addAchievementItem(row1, "⭐", "Level 5", currentLevel >= 5);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, 16);

        addAchievementItem(row2, "📖", "Collector", discoveredSpecies >= 3);
        addAchievementItem(row2, "✨", "Perfect Master", perfectThrows >= 10);

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
                achievementPrefs.edit().clear().apply();
                loadAchievementStates();
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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(24, 16, 24, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardElevation(12f);
        card.setCardBackgroundColor(Color.parseColor("#1A1A1A"));  // Black bg

        // Legacy CardView stroke (no setStrokeColor - use background drawable)
        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setColor(Color.parseColor("#1A1A1A"));              // Card color
        stroke.setStroke(3, Color.parseColor("#FF9800"));          // Orange stroke!
        stroke.setCornerRadius(24f);
        card.setBackground(stroke);

        return card;
    }
}