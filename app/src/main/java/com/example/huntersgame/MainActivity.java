package com.example.huntersgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnProfile;
    private Button btnMap;  // <-- ADD THIS LINE

    private Button btnChallenges;
    private Button btnBestiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnProfile = findViewById(R.id.btn_profile);
        btnMap = findViewById(R.id.btn_map);  // <-- ADD THIS LINE
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
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // ADD THIS MAP BUTTON CLICK LISTENER
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }
}