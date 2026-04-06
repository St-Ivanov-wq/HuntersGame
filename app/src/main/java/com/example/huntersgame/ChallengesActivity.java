package com.example.huntersgame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

public class ChallengesActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private LinearLayout challengesContainer;
    private TextView tvTotalRewards;
    private Button btnClaimAll;
    private Button btnReset;
    private Button btnBack;
    private List<ChallengeEntity> allChallenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        challengesContainer = findViewById(R.id.challenges_container);
        tvTotalRewards = findViewById(R.id.tv_total_rewards);
        btnClaimAll = findViewById(R.id.btn_claim_all);
        btnReset = findViewById(R.id.btn_reset_challenges);
        btnBack = findViewById(R.id.btn_back_challenges);

        // Check if buttons exist (for safety)
        if (btnBack == null) {
            Log.e("ChallengesActivity", "btn_back_challenges not found!");
            finish();
            return;
        }

        dbManager = new DatabaseManager(this);

        btnBack.setOnClickListener(v -> finish());

        if (btnClaimAll != null) {
            btnClaimAll.setOnClickListener(v -> claimAllRewards());
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> showResetDialog());
        }

        loadChallenges();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChallenges();
    }

    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Challenges")
                .setMessage("Reset today's challenges and get new ones? (Progress will be lost)")
                .setPositiveButton("Reset", (dialog, which) -> resetChallenges())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetChallenges() {
        dbManager.manualResetChallenges(new DatabaseManager.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(ChallengesActivity.this, "New challenges generated!", Toast.LENGTH_SHORT).show();
                loadChallenges();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChallengesActivity.this, "Error resetting: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChallenges() {
        dbManager.getAllChallenges(new DatabaseManager.Callback<List<ChallengeEntity>>() {
            @Override
            public void onSuccess(List<ChallengeEntity> challenges) {
                allChallenges = challenges;
                displayChallenges();
            }

            @Override
            public void onError(String error) {
                Log.e("ChallengesActivity", "Error loading challenges: " + error);
                Toast.makeText(ChallengesActivity.this, "Error loading challenges", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayChallenges() {
        challengesContainer.removeAllViews();

        int totalRewards = 0;

        if (allChallenges != null) {
            for (ChallengeEntity challenge : allChallenges) {
                if (challenge.isCompleted() && !challenge.isClaimed()) {
                    totalRewards += challenge.getRewardXP();
                }
                addChallengeCard(challenge);
            }
        }

        tvTotalRewards.setText("⭐ " + totalRewards + " XP available");

        if (totalRewards == 0 && allChallenges != null && allChallenges.size() > 0) {
            boolean allComplete = true;
            for (ChallengeEntity c : allChallenges) {
                if (!c.isCompleted()) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) {
                tvTotalRewards.setText("🎉 ALL CHALLENGES COMPLETE! 🎉");
            }
        }
    }

    private void addChallengeCard(ChallengeEntity challenge) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(20, 12, 20, 12);
        card.setLayoutParams(cardParams);
        card.setRadius(20f);
        card.setCardElevation(8f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(30, 25, 30, 25);

        // Status icon
        TextView statusText = new TextView(this);
        statusText.setTextSize(24);
        if (challenge.isCompleted()) {
            if (challenge.isClaimed()) {
                statusText.setText("✓");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusText.setText("!");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        } else {
            statusText.setText("○");
            statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Text container
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setPadding(20, 0, 0, 0);

        TextView nameText = new TextView(this);
        nameText.setText(challenge.getName());
        nameText.setTextSize(16);
        nameText.setTextColor(getResources().getColor(android.R.color.white));

        TextView progressText = new TextView(this);
        if (challenge.isCompleted()) {
            progressText.setText("COMPLETE! +" + challenge.getRewardXP() + " XP");
            progressText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            progressText.setText("Progress: " + challenge.getProgress() + "/" + challenge.getTarget());
            progressText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        progressText.setTextSize(12);
        progressText.setPadding(0, 4, 0, 0);

        textLayout.addView(nameText);
        textLayout.addView(progressText);

        layout.addView(statusText);
        layout.addView(textLayout);

        // Reward badge
        TextView rewardBadge = new TextView(this);
        rewardBadge.setText("+" + challenge.getRewardXP());
        rewardBadge.setTextSize(14);
        rewardBadge.setTextColor(Color.parseColor("#FFD700"));
        rewardBadge.setPadding(16, 0, 0, 0);

        layout.addView(rewardBadge);

        card.addView(layout);
        challengesContainer.addView(card);
    }

    private void claimAllRewards() {
        dbManager.claimAllRewards(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer totalXP) {
                if (totalXP > 0) {
                    dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
                        @Override
                        public void onSuccess(Integer currentXP) {
                            dbManager.saveXP(currentXP + totalXP, new DatabaseManager.Callback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    Toast.makeText(ChallengesActivity.this, "Claimed " + totalXP + " XP!", Toast.LENGTH_LONG).show();
                                    loadChallenges();
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("ChallengesActivity", "Error saving XP: " + error);
                                    Toast.makeText(ChallengesActivity.this, "Error saving XP", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("ChallengesActivity", "Error getting XP: " + error);
                            Toast.makeText(ChallengesActivity.this, "Error getting XP", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChallengesActivity.this, "No rewards to claim yet!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ChallengesActivity", "Error claiming rewards: " + error);
                Toast.makeText(ChallengesActivity.this, "Error claiming rewards", Toast.LENGTH_SHORT).show();
            }
        });
    }
}