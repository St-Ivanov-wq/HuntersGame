package com.example.huntersgame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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

public class BestiaryActivity extends AppCompatActivity {

    private LinearLayout creaturesContainer;
    private Button btnBack;
    private TextView tvTotalCount;
    private DatabaseManager dbManager;
    private List<CreatureEntity> allCreatures;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bestiary);

        creaturesContainer = findViewById(R.id.creatures_container);
        btnBack = findViewById(R.id.btn_back_bestiary);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        dbManager = new DatabaseManager(this);
        allCreatures = new ArrayList<>();

        btnBack.setOnClickListener(v -> finish());

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
        cardParams.setMargins(20, 16, 20, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(30f);
        card.setCardElevation(10f);
        card.setUseCompatPadding(true);

        boolean hasCaptured = creature.getCaptureCount() > 0;

        if (hasCaptured) {
            card.setCardBackgroundColor(Color.parseColor("#2D2D44"));
            card.setAlpha(1f);
        } else {
            card.setCardBackgroundColor(Color.parseColor("#1A1A2E"));
            card.setAlpha(0.85f);
        }

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(35, 35, 35, 35);
        contentLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Creature Image
        ImageView creatureImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(180, 180);
        imageParams.setMargins(0, 0, 30, 0);
        creatureImage.setLayoutParams(imageParams);
        creatureImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int imageResId = creature.getImageResId();
        if (imageResId != 0) {
            creatureImage.setImageResource(imageResId);
        } else {
            creatureImage.setImageResource(android.R.drawable.star_on);
        }

        if (!hasCaptured) {
            creatureImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            creatureImage.setAlpha(0.4f);
        } else {
            creatureImage.setColorFilter(null);
            creatureImage.setAlpha(1f);
        }

        // Text Container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        // Name
        TextView nameText = new TextView(this);
        nameText.setTextSize(18);
        nameText.setTypeface(null, Typeface.BOLD);

        if (hasCaptured) {
            nameText.setText(creature.getName() + " (" + creature.getNameBulgarian() + ")");
            nameText.setTextColor(Color.parseColor("#FFD700"));
        } else {
            nameText.setText("???");
            nameText.setTextColor(Color.parseColor("#666666"));
        }

        // Capture count info
        TextView infoText = new TextView(this);
        infoText.setTextSize(12);
        infoText.setPadding(0, 8, 0, 0);

        if (hasCaptured) {
            infoText.setText("Captured " + creature.getCaptureCount() + " time" + (creature.getCaptureCount() != 1 ? "s" : ""));
            infoText.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            infoText.setText("Not yet discovered");
            infoText.setTextColor(Color.parseColor("#444444"));
        }

        textContainer.addView(nameText);
        textContainer.addView(infoText);
        contentLayout.addView(creatureImage);
        contentLayout.addView(textContainer);

        // Badge for captured
        if (hasCaptured) {
            TextView badge = new TextView(this);
            badge.setText("✓");
            badge.setTextSize(24);
            badge.setTextColor(Color.parseColor("#FFD700"));
            contentLayout.addView(badge);
        }

        card.addView(contentLayout);

        card.setOnClickListener(v -> {
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

        int imageResId = creature.getImageResId();
        if (imageResId != 0) {
            ivSprite.setImageResource(imageResId);
        } else {
            ivSprite.setImageResource(android.R.drawable.star_on);
        }

        TextView tvName = dialogView.findViewById(R.id.dialog_creature_name);
        TextView tvNameBulgarian = dialogView.findViewById(R.id.dialog_creature_name_bulgarian);
        TextView tvDescription = dialogView.findViewById(R.id.dialog_creature_description);
        TextView tvHabitat = dialogView.findViewById(R.id.dialog_creature_habitat);
        TextView tvPoints = dialogView.findViewById(R.id.dialog_creature_points);
        TextView tvCaptureInfo = dialogView.findViewById(R.id.dialog_capture_info);
        TextView tvRank = dialogView.findViewById(R.id.dialog_rank);

        tvName.setText(creature.getName());
        tvNameBulgarian.setText(creature.getNameBulgarian());
        tvPoints.setText("XP Value: " + creature.getPointsValue());
        tvDescription.setText(creature.getDisplayDescription());
        tvHabitat.setText("Habitat: " + creature.getDisplayHabitat());
        tvCaptureInfo.setText("Captured " + creature.getCaptureCount() + " time" + (creature.getCaptureCount() != 1 ? "s" : ""));
        tvRank.setText(creature.getHunterRank());

        // Set rank color
        if (creature.getCaptureCount() >= 10) {
            tvRank.setTextColor(Color.parseColor("#FFD700"));
        } else if (creature.getCaptureCount() >= 5) {
            tvRank.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (creature.getCaptureCount() >= 2) {
            tvRank.setTextColor(Color.parseColor("#CD7F32"));
        } else {
            tvRank.setTextColor(Color.parseColor("#666666"));
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