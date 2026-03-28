package com.example.huntersgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvHunterName;
    private TextView tvTotalPoints;
    private TextView tvCapturedCount;
    private TextView tvRemainingCount;
    private Button btnBack;
    private Button btnReset;

    private SaveManager saveManager;
    private List<Creature> allCreatures;
    private int totalPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {
            tvHunterName = findViewById(R.id.tv_hunter_name);
            tvTotalPoints = findViewById(R.id.tv_total_points);
            tvCapturedCount = findViewById(R.id.tv_captured_count);
            tvRemainingCount = findViewById(R.id.tv_remaining_count);
            btnBack = findViewById(R.id.btn_back);
            btnReset = findViewById(R.id.btn_reset);

            // Initialize SaveManager
            saveManager = new SaveManager(this);

            // Get creatures
            allCreatures = CreatureData.getCreatures();

            // Load saved data
            loadSavedData();

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            btnReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetProgress();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedData() {
        try {
            // Load saved points
            totalPoints = saveManager.loadPoints();

            // Load saved creature states
            List<Creature> savedCreatures = saveManager.loadCreatures();
            if (savedCreatures != null && savedCreatures.size() == allCreatures.size()) {
                for (int i = 0; i < allCreatures.size(); i++) {
                    allCreatures.get(i).setCaptured(savedCreatures.get(i).isCaptured());
                }
            }

            updateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDisplay() {
        int capturedCount = 0;
        for (Creature creature : allCreatures) {
            if (creature.isCaptured()) {
                capturedCount++;
            }
        }

        tvHunterName.setText("🏹 Master Hunter");
        tvTotalPoints.setText("⭐ Total Points: " + totalPoints);
        tvCapturedCount.setText("📖 Captured: " + capturedCount + " / " + allCreatures.size());
        tvRemainingCount.setText("🔍 Remaining: " + (allCreatures.size() - capturedCount));

        // Show completion message
        if (capturedCount == allCreatures.size()) {
            tvHunterName.setText("🏆 LEGENDARY HUNTER! 🏆");
            Toast.makeText(this, "Congratulations! You've captured all mythical creatures!", Toast.LENGTH_LONG).show();
        }
    }

    private void resetProgress() {
        try {
            // Reset all creatures
            for (Creature creature : allCreatures) {
                creature.setCaptured(false);
            }
            totalPoints = 0;

            // Save reset data
            saveManager.saveCreatures(allCreatures);
            saveManager.savePoints(0);

            updateDisplay();
            Toast.makeText(this, "Progress reset! Start hunting again!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error resetting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}