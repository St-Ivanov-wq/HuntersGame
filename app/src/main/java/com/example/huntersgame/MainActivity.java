package com.example.huntersgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnProfile;
    private Button btnMap;
    private Button btnChallenges;
    private Button btnBestiary;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SoundManager
        SoundManager.getInstance(this);

        // Play app launch sound with music delay
        playAppLaunchWithMusicDelay();

        btnProfile = findViewById(R.id.btn_profile);
        btnMap = findViewById(R.id.btn_map);
        btnBestiary = findViewById(R.id.btn_bestiary);
        btnChallenges = findViewById(R.id.btn_challenges);
        btnSettings = findViewById(R.id.btn_settings);
        AchievementManager.getInstance(this);


        btnChallenges.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, ChallengesActivity.class);
            startActivity(intent);
        });

        btnBestiary.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, BestiaryActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            SoundManager.getInstance(MainActivity.this).playButtonClick();
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnMap.setOnClickListener(v -> {
            SoundManager.getInstance(MainActivity.this).playButtonClick();
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            SoundManager.getInstance(MainActivity.this).playButtonClick();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void playAppLaunchWithMusicDelay() {
        SoundManager sm = SoundManager.getInstance(this);
        sm.playAppLaunch();
        new Handler().postDelayed(() -> sm.playMainTheme(), 2000);
    }
}