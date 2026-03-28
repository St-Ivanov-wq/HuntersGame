package com.example.huntersgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStartGame;
    private Button btnCollection;
    private Button btnProfile;
    private Button btnMap;  // <-- ADD THIS LINE
    private Button btnAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartGame = findViewById(R.id.btn_start_game);
        btnCollection = findViewById(R.id.btn_collection);
        btnProfile = findViewById(R.id.btn_profile);
        btnMap = findViewById(R.id.btn_map);  // <-- ADD THIS LINE
        btnAR = findViewById(R.id.btn_ar);

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
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

       // btnAR.setOnClickListener(new View.OnClickListener() {
         //   @Override
           // public void onClick(View v) {
             //   Intent intent = new Intent(MainActivity.this, ARActivity.class);
               // startActivity(intent);
            //}
        //});
    }
}