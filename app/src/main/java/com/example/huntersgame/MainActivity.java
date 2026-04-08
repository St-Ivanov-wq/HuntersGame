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
    private Button btnTestSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SoundManager
        SoundManager.getInstance(this);

        // Play app launch sound, then music after delay
        playAppLaunchWithMusicDelay();

        btnProfile = findViewById(R.id.btn_profile);
        btnMap = findViewById(R.id.btn_map);
        btnBestiary = findViewById(R.id.btn_bestiary);
        btnChallenges = findViewById(R.id.btn_challenges);

        btnChallenges.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChallengesActivity.class);
            startActivity(intent);
        });

        btnBestiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BestiaryActivity.class);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.getInstance(MainActivity.this).playButtonClick();
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.getInstance(MainActivity.this).playButtonClick();
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void playAppLaunchWithMusicDelay() {
        SoundManager sm = SoundManager.getInstance(this);

        // Play the launch sound
        sm.playAppLaunch();

        // Delay the main theme by 2 seconds (or you can use sound duration)
        new Handler().postDelayed(() -> {
            sm.playMainTheme();
        }, 900); // 2000 milliseconds = 2 seconds delay
    }
}