package com.example.huntersgame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.Random;

public class GameActivity extends BaseActivity {

    private TextView tvLocationStatus;
    private TextView tvCreatureFound;
    private Button btnFindCreatures;
    private Button btnCapture;
    private Button btnBack;

    private FusedLocationProviderClient fusedLocationClient;
    private List<Creature> allCreatures;
    private Creature currentCreature;
    private int currentPoints = 0;
    private TextView tvPoints;

    private SaveManager saveManager;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // Initialize views
            tvLocationStatus = findViewById(R.id.tv_location_status);
            tvCreatureFound = findViewById(R.id.tv_creature_found);
            btnFindCreatures = findViewById(R.id.btn_find_creatures);
            btnCapture = findViewById(R.id.btn_capture);
            btnBack = findViewById(R.id.btn_back);
            tvPoints = findViewById(R.id.tv_points);

            // Initialize SaveManager
            saveManager = new SaveManager(this);

            // Get all creatures from our data source
            allCreatures = CreatureData.getCreatures();

            // Load saved points
            currentPoints = saveManager.loadPoints();
            updatePointsDisplay();

            // Load saved creature data
            List<Creature> savedCreatures = saveManager.loadCreatures();
            if (savedCreatures != null && savedCreatures.size() == allCreatures.size()) {
                // Update our creatures with saved data
                for (int i = 0; i < allCreatures.size(); i++) {
                    allCreatures.get(i).setCaptured(savedCreatures.get(i).isCaptured());
                }
            }

            // Initialize location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Set button click listeners
            btnFindCreatures.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findNearbyCreatures();
                }
            });

            btnCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    captureCreature();
                }
            });

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            // Initially disable capture button
            btnCapture.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting game: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void findNearbyCreatures() {
        try {
            // Check if we have location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            // Get current location
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        tvLocationStatus.setText("📍 Location found!\nLat: " +
                                String.format("%.4f", latitude) + ", Lng: " +
                                String.format("%.4f", longitude));

                        // Find a random creature that hasn't been captured yet
                        findRandomCreature();

                    } else {
                        tvLocationStatus.setText("⚠️ Could not get location. Try again!");
                        Toast.makeText(GameActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void findRandomCreature() {
        try {
            // Get list of uncaptured creatures
            java.util.ArrayList<Creature> uncapturedCreatures = new java.util.ArrayList<>();
            for (Creature creature : allCreatures) {
                if (!creature.isCaptured()) {
                    uncapturedCreatures.add(creature);
                }
            }

            if (uncapturedCreatures.isEmpty()) {
                tvCreatureFound.setText("🎉 Congratulations!\nYou've captured all mythical creatures!");
                btnFindCreatures.setEnabled(false);
                btnCapture.setEnabled(false);
                return;
            }

            // Pick a random creature
            Random random = new Random();
            int index = random.nextInt(uncapturedCreatures.size());
            currentCreature = uncapturedCreatures.get(index);

            // Display the creature
            String creatureInfo = "✨ You found a " + currentCreature.getName() + " (" +
                    currentCreature.getNameBulgarian() + ")!\n\n" +
                    "📖 " + currentCreature.getDescription() + "\n\n" +
                    "🏞️ Habitat: " + currentCreature.getHabitat() + "\n" +
                    "⭐ Worth: " + currentCreature.getPointsValue() + " points";

            tvCreatureFound.setText(creatureInfo);
            btnCapture.setEnabled(true);
            btnFindCreatures.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding creature: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void captureCreature() {
        try {

            if (currentCreature != null && !currentCreature.isCaptured()) {
                // Capture the creature
                currentCreature.setCaptured(true);
                currentCreature.incrementCaptureCount();  // NEW: increment capture count
                currentPoints += currentCreature.getPointsValue();

                // Save after capturing
                saveManager.saveCreatures(allCreatures);
                saveManager.savePoints(currentPoints);

                // Update display
                updatePointsDisplay();

                String captureMessage = "✅ Captured " + currentCreature.getName() + "!\n" +
                        "🏆 +" + currentCreature.getPointsValue() + " points!\n\n" +
                        "Press 'Find Creatures' to hunt again!";
                tvCreatureFound.setText(captureMessage);

                Toast.makeText(this, "You captured " + currentCreature.getName() + "!", Toast.LENGTH_LONG).show();

                // Reset for next hunt
                btnCapture.setEnabled(false);
                btnFindCreatures.setEnabled(true);
                currentCreature = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error capturing creature: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsDisplay() {
        tvPoints.setText("⭐ Points: " + currentPoints);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try finding creatures again
                findNearbyCreatures();
            } else {
                Toast.makeText(this, "Location permission needed to find creatures!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}