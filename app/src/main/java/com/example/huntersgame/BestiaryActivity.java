package com.example.huntersgame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class BestiaryActivity extends BaseActivity{

    private LinearLayout creaturesContainer;
    private Button btnBack;
    private TextView tvTotalCount;
    private DatabaseManager dbManager;
    private List<CreatureEntity> allCreatures;
    private TextView tvEmptyMessage;
    private VibrationManager vibrationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bestiary);

        // Play menu open sound AFTER super and setContentView
        SoundManager.getInstance(this).playMenuOpen();

        creaturesContainer = findViewById(R.id.creatures_container);
       btnBack = findViewById(R.id.btn_back_bestiary);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        vibrationManager = new VibrationManager(this);


        dbManager = new DatabaseManager(this);
        allCreatures = new ArrayList<>();

        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playMenuClose();
            finish();
        });

        loadCreaturesFromDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCreaturesFromDatabase();
    }

    private void loadCreaturesFromDatabase() {
        dbManager.getAllCreatures(new DatabaseManager.Callback<List<CreatureEntity>>() {
            @Override
            public void onSuccess(List<CreatureEntity> creatures) {
                android.util.Log.d("Bestiary", "=== LOADED " + creatures.size() + " CREATURES ===");
                for (CreatureEntity c : creatures) {
                    android.util.Log.d("Bestiary", "Creature: " + c.getName());
                }
                allCreatures = creatures;
                displayBestiary();
            }

            @Override
            public void onError(String error) {
                Log.e("Bestiary", "Error loading creatures: " + error);
                Toast.makeText(BestiaryActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBestiary() {
        creaturesContainer.removeAllViews();

        int discoveredCount = 0;
        for (CreatureEntity creature : allCreatures) {
            if (creature.getCaptureCount() > 0) {
                discoveredCount++;
            }
            addCreatureCard(creature);
        }

        tvTotalCount.setText(discoveredCount + " / " + allCreatures.size() + " SPECIES DISCOVERED");
        tvEmptyMessage.setVisibility(discoveredCount == 0 ? View.VISIBLE : View.GONE);
    }

    private void addCreatureCard(CreatureEntity creature) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(24, 16, 24, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(28f);
        card.setCardElevation(16f);
        card.setUseCompatPadding(true);

        // Forest card bg + gold stroke
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setShape(GradientDrawable.RECTANGLE);
        cardBg.setColor(Color.parseColor("#2A4F3A"));
        cardBg.setStroke(3, Color.parseColor("#FFD700"));
        cardBg.setCornerRadius(28f);
        card.setBackground(cardBg);

        boolean hasCaptured = creature.getCaptureCount() > 0;
        boolean isMaster = creature.getCaptureCount() >= 10;

        if (!hasCaptured) {
            card.setAlpha(0.85f);
        }

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(36, 36, 36, 36);
        contentLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Creature Image
        ImageView creatureImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(140, 140);
        imageParams.setMargins(0, 0, 28, 0);
        creatureImage.setLayoutParams(imageParams);
        creatureImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Load image
        String imageName = creature.getName().toLowerCase().replace(" ", "");
        int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());

        if (imageResId != 0) {
            creatureImage.setImageResource(imageResId);
        } else {
            creatureImage.setImageResource(android.R.drawable.star_on);
        }

        if (!hasCaptured) {
            creatureImage.setColorFilter(Color.parseColor("#666666"), PorterDuff.Mode.SRC_IN);
            creatureImage.setAlpha(0.6f);
        } else {
            creatureImage.setColorFilter(null);
            creatureImage.setAlpha(1f);
        }

        // Text Container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Name Layout (with master badge)
        LinearLayout nameLayout = new LinearLayout(this);
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);
        nameLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameText = new TextView(this);
        nameText.setTextSize(20);
        nameText.setTypeface(null, Typeface.BOLD);

        if (hasCaptured) {
            nameText.setText(creature.getName() + " (" + creature.getNameBulgarian() + ")");
            nameText.setTextColor(Color.parseColor("#FFD700"));
        } else {
            nameText.setText("???");
            nameText.setTextColor(Color.parseColor("#D4AF37"));
        }

        nameLayout.addView(nameText);

        // Master trophy badge
//        if (isMaster) {
//            TextView masterBadge = new TextView(this);
////            masterBadge.setText(" 🏆");
////            masterBadge.setTextSize(20);
//            masterBadge.setTextColor(Color.parseColor("#FFD700"));
//            masterBadge.setShadowLayer(4, 1, 1, Color.parseColor("#FF5722"));
//            masterBadge.setPadding(8, 0, 0, 0);
//            nameLayout.addView(masterBadge);
//        }

        textContainer.addView(nameLayout);

        // Capture count info
        TextView infoText = new TextView(this);
        infoText.setTextSize(14);
        infoText.setPadding(0, 12, 0, 0);

        if (hasCaptured) {
            if (isMaster) {
                infoText.setText("Captured × " + creature.getCaptureCount());
                infoText.setTextColor(Color.parseColor("#FFD700"));
                infoText.setTypeface(null, Typeface.BOLD);
            } else {
                infoText.setText("Captured × " + creature.getCaptureCount());
                infoText.setTextColor(Color.parseColor("#FF8C00"));
            }
        } else {
            infoText.setText("Not yet discovered");
            infoText.setTextColor(Color.parseColor("#A8A8A8"));
        }

        textContainer.addView(infoText);

        contentLayout.addView(creatureImage);
        contentLayout.addView(textContainer);

        // Trophy or checkmark on the right
        if (isMaster) {
            TextView trophy = new TextView(this);
            trophy.setText("🏆");
            trophy.setTextSize(40);
            trophy.setTextColor(Color.parseColor("#FFD700"));
            trophy.setShadowLayer(6, 2, 2, Color.parseColor("#FF5722"));
            contentLayout.addView(trophy);
        }
        card.addView(contentLayout);

        card.setOnClickListener(v -> {
            SoundManager.getInstance(this).playCardFlip();
            if (hasCaptured) {
                showCreatureDetails(creature);
            } else {
                Toast.makeText(this, "Capture this creature first to learn about it!", Toast.LENGTH_SHORT).show();
            }
        });

        creaturesContainer.addView(card);
    }

    private void showCreatureDetails(CreatureEntity creature) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_creature_details, null);

        ImageView ivSprite = dialogView.findViewById(R.id.dialog_creature_sprite);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(350, 350);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.bottomMargin = 24;
        ivSprite.setLayoutParams(layoutParams);
        ivSprite.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Load creature image
        String imageName = creature.getName().toLowerCase().replace(" ", "");
        int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (imageResId != 0) {
            ivSprite.setImageResource(imageResId);
        } else {
            ivSprite.setImageResource(android.R.drawable.star_on);
        }

        int captureCount = creature.getCaptureCount();
        boolean isMaster = captureCount >= 10;

        TextView tvName = dialogView.findViewById(R.id.dialog_creature_name);
        TextView tvNameBulgarian = dialogView.findViewById(R.id.dialog_creature_name_bulgarian);
        TextView tvDescription = dialogView.findViewById(R.id.dialog_creature_description);
        TextView tvHabitat = dialogView.findViewById(R.id.dialog_creature_habitat);
        TextView tvPoints = dialogView.findViewById(R.id.dialog_creature_points);
        TextView tvCaptureInfo = dialogView.findViewById(R.id.dialog_capture_info);
        TextView tvMasterBadge = dialogView.findViewById(R.id.dialog_master_badge);
        TextView tvHunterRank = dialogView.findViewById(R.id.dialog_rank);

        tvName.setText(creature.getName());
        tvNameBulgarian.setText(creature.getNameBulgarian());
        tvPoints.setText("XP Value: " + creature.getPointsValue());

        // Show description and habitat based on unlock level
        if (captureCount >= 5) {
            tvDescription.setText(creature.getDescription());
            tvHabitat.setText("Habitat: " + creature.getHabitat());
            tvHunterRank.setText("EXPERT HUNTER");
            tvHunterRank.setTextColor(Color.parseColor("#8B5CF6"));
        } else if (captureCount >= 2) {
            tvDescription.setText("??? (Capture " + (5 - captureCount) + " more to unlock description)");
            tvHabitat.setText("Habitat: " + creature.getHabitat());
            tvHunterRank.setText("VETERAN HUNTER");
            tvHunterRank.setTextColor(Color.parseColor("#3B82F6"));
        } else {
            tvDescription.setText("??? (Capture " + (5 - captureCount) + " more to unlock)");
            tvHabitat.setText("Habitat: ???");
        }

        // Capture info with mastery badge
        if (isMaster) {
            tvCaptureInfo.setText("🏆 MASTER HUNTER 🏆\nCaptured " + captureCount + " times");
            tvCaptureInfo.setTextColor(Color.parseColor("#FFD700"));
            tvCaptureInfo.setTextSize(14);
            tvCaptureInfo.setTypeface(null, Typeface.BOLD);
            tvHunterRank.setText("MASTER HUNTER");
            tvHunterRank.setTextColor(Color.parseColor("#F59E0B"));
        } else if (captureCount > 0) {
            tvCaptureInfo.setText("Captured " + captureCount + " time" + (captureCount != 1 ? "s" : ""));
            tvCaptureInfo.setTextColor(Color.parseColor("#AAAAAA"));
            tvCaptureInfo.setTextSize(12);
            tvCaptureInfo.setTypeface(null, Typeface.NORMAL);

            if (tvMasterBadge != null) {
                tvMasterBadge.setVisibility(View.GONE);
            }
        } else {
            tvCaptureInfo.setText("Not yet discovered");
            tvCaptureInfo.setTextColor(Color.parseColor("#666666"));

            if (tvMasterBadge != null) {
                tvMasterBadge.setVisibility(View.GONE);
            }
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btn_close_dialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}