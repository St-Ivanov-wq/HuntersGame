package com.example.huntersgame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import java.util.List;

public class ChallengesActivity extends BaseActivity {

    private DatabaseManager dbManager;
    private LinearLayout challengesContainer;
    private TextView tvTotalRewards;
    private Button btnClaimAll;
    private Button btnReset;
    private Button btnBack;
    private List<ChallengeEntity> allChallenges;

    private VibrationManager vibrationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SoundManager.getInstance(this).playMenuOpen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        challengesContainer = findViewById(R.id.challenges_container);
        tvTotalRewards = findViewById(R.id.tv_total_rewards);
        btnClaimAll = findViewById(R.id.btn_claim_all);
        btnReset = findViewById(R.id.btn_reset_challenges);
        btnBack = findViewById(R.id.btn_back_challenges);
        vibrationManager = new VibrationManager(this);

        // Check if buttons exist (for safety)
        if (btnBack == null) {
            Log.e("ChallengesActivity", "btn_back_challenges not found!");
            SoundManager.getInstance(this).playMenuClose();
            finish();
            return;
        }

        dbManager = DatabaseManager.getInstance(this);

        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playMenuClose();
            finish();});

        if (btnClaimAll != null) {
            btnClaimAll.setOnClickListener(v -> {SoundManager.getInstance(this).playButtonClick();
                claimAllRewards();});
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                SoundManager.getInstance(this).playButtonClick();
                showResetDialog();});
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
        vibrationManager.tap();
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
        cardParams.setMargins(24, 16, 24, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardElevation(12f);
        card.setCardBackgroundColor(Color.parseColor("#1A1A1A"));  // Black card

        // Stroke effect (legacy CardView)
        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setColor(Color.parseColor("#1A1A1A"));
        stroke.setStroke(2, Color.parseColor("#FF5722"));  // Red stroke!
        stroke.setCornerRadius(24f);
        card.setBackground(stroke);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        // Status Icon (Themed)
        TextView statusText = new TextView(this);
        statusText.setTextSize(28);
        if (challenge.isCompleted()) {
            if (challenge.isClaimed()) {
                statusText.setText("✅");
                statusText.setTextColor(Color.parseColor("#4CAF50"));  // Green complete
                statusText.setShadowLayer(2, 1, 1, Color.parseColor("#FFD700"));
            } else {
                statusText.setText("⭐");
                statusText.setTextColor(Color.parseColor("#FF9800"));  // Orange ready!
                statusText.setShadowLayer(2, 1, 1, Color.parseColor("#FFD700"));
            }
        } else {
            statusText.setText("⚪");
            statusText.setTextColor(Color.parseColor("#666666"));    // Gray pending
        }

        // Challenge Name (Gold)
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setPadding(24, 0, 0, 0);

        TextView nameText = new TextView(this);
        nameText.setText(challenge.getName());
        nameText.setTextSize(18);
        nameText.setTextColor(Color.parseColor("#FFD700"));      // Gold title
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setShadowLayer(1, 1, 1, Color.parseColor("#1A1A1A"));

        // Progress (Orange/Red)
        TextView progressText = new TextView(this);
        if (challenge.isCompleted()) {
            progressText.setText("COMPLETE! +" + challenge.getRewardXP() + " XP");
            progressText.setTextColor(Color.parseColor("#FF5722"));  // Red complete
        } else {
            progressText.setText("Progress: " + challenge.getProgress() + "/" + challenge.getTarget());
            progressText.setTextColor(Color.parseColor("#FF9800"));  // Orange active
        }
        progressText.setTextSize(14);
        progressText.setPadding(0, 8, 0, 0);

        textLayout.addView(nameText);
        textLayout.addView(progressText);

        // Reward Badge (Fiery Gold)
        TextView rewardBadge = new TextView(this);
        rewardBadge.setText("+" + challenge.getRewardXP() + " XP");
        rewardBadge.setTextSize(16);
        rewardBadge.setTextColor(Color.parseColor("#FFD700"));    // Gold XP
        rewardBadge.setTypeface(null, Typeface.BOLD);
        rewardBadge.setShadowLayer(3, 1, 1, Color.parseColor("#FF5722")); // Red glow!
        rewardBadge.setPadding(24, 0, 0, 0);

        layout.addView(statusText);
        layout.addView(textLayout);
        layout.addView(rewardBadge);

        card.addView(layout);
        challengesContainer.addView(card);
    }

    private void claimAllRewards() {
        vibrationManager.success();
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