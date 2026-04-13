package com.example.huntersgame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.List;

public class ARActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private ImageView creatureSprite;
    private TextView tvCreatureName;
    private TextView tvCreaturePoints;
    private TextView tvStatus;
    private Button btnCancel;
    private Button btnThrow;
    private ImageView netImageView;
    private TextView powerMeterText;
    private View powerBar;
    private FrameLayout spriteContainer;
    private TextView tvCombo;

    private VibrationManager vibrationManager;

    private List<CreatureEntity> allCreatures;
    private DatabaseManager dbManager;

    // Gyroscope variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    // Calibration
    private float calibrationX = 0;
    private float calibrationY = 0;
    private boolean calibrated = false;
    private int calibrationSamples = 0;

    // Sprite movement
    private float spriteX = 0;
    private float spriteY = 0;
    private int containerWidth = 0;
    private int containerHeight = 0;
    private int spriteWidth = 0;
    private int spriteHeight = 0;
    private boolean spriteActive = true;

    // Throw mechanics
    private boolean isThrowing = false;
    private boolean canThrow = true;
    private int powerLevel = 0;
    private boolean powerIncreasing = true;
    private Handler powerHandler = new Handler();
    private int hitCount = 0;
    private int requiredHits = 3;
    private int powerBarMaxWidth = 0;

    // Combo system
    private int currentCombo = 0;
    private int bestCombo = 0;
    private int currentLevel = 1;

    private CreatureEntity currentCreature;

    private static final int CAMERA_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SoundManager.getInstance(this).playEncounterCreature();
        SoundManager.getInstance(this).stopMainTheme();
        SoundManager.getInstance(this).playHuntingTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_simple);

        // Initialize views
        surfaceView = findViewById(R.id.surface_view);
        creatureSprite = findViewById(R.id.creature_sprite);
        tvCreatureName = findViewById(R.id.tv_creature_name);
        tvCreaturePoints = findViewById(R.id.tv_creature_points);
        tvStatus = findViewById(R.id.tv_ar_status);
        btnCancel = findViewById(R.id.btn_ar_back);
        btnThrow = findViewById(R.id.btn_throw);
        netImageView = findViewById(R.id.net_image);
        powerMeterText = findViewById(R.id.power_meter_text);
        powerBar = findViewById(R.id.power_bar);
        spriteContainer = findViewById(R.id.sprite_container);
        tvCombo = findViewById(R.id.tv_combo);
        vibrationManager = new VibrationManager(this);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        // Initialize Database Manager
        dbManager = new DatabaseManager(this);

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        // Get creature from intent
        Intent intent = getIntent();
        String creatureName = intent.getStringExtra("creature_name");
        int creatureXP = intent.getIntExtra("creature_xp", 0);
        int incomingCombo = intent.getIntExtra("current_combo", 0);

        // Set combo from previous session
        currentCombo = incomingCombo;
        Log.d("COMBO", "Starting with combo: " + currentCombo);

        // Initialize all creatures
        loadAllCreatures();

        // Create creature object
        currentCreature = new CreatureEntity("temp", creatureName, "", "", "", creatureXP, 0);

        // Set up UI
        tvCreatureName.setText(creatureName);
        tvCreaturePoints.setText(creatureXP + " XP");

        // Load creature image
        if (creatureName != null && !creatureName.isEmpty()) {
            int imageResId = getResources().getIdentifier(
                    creatureName.toLowerCase(), "drawable", getPackageName());
            if (imageResId != 0) {
                creatureSprite.setImageResource(imageResId);
            } else {
                createColoredCircle(creatureName);
            }
        }

        creatureSprite.setVisibility(View.VISIBLE);

        // Hide net initially
        netImageView.setVisibility(View.GONE);

        // Update combo display
        updateComboDisplay();

        tvStatus.setText("MOVE YOUR PHONE");
        tvStatus.setTextSize(18);
        tvStatus.setTextColor(Color.parseColor("#FFD700"));

        // Set up throw button
        btnThrow.setOnClickListener(v -> {
            if (canThrow && !isThrowing) {
                throwNet();
            }
        });

        btnCancel.setOnClickListener(v -> {
            SoundManager.getInstance(ARActivity.this).playButtonClick();
            setResult(RESULT_CANCELED);
            finish();
        });

        // Get power bar max width
        powerBar.post(() -> powerBarMaxWidth = powerBar.getWidth());

        // Get sprite container dimensions
        spriteContainer.post(() -> {
            containerWidth = spriteContainer.getWidth();
            containerHeight = spriteContainer.getHeight();
            spriteWidth = creatureSprite.getWidth();
            spriteHeight = creatureSprite.getHeight();

            if (containerWidth > 0 && containerHeight > 0 && spriteWidth > 0 && spriteHeight > 0) {
                spriteX = (containerWidth - spriteWidth) / 2;
                spriteY = (containerHeight - spriteHeight) / 2;
                creatureSprite.setX(spriteX);
                creatureSprite.setY(spriteY);
            }
        });

        // Start power meter
        startPowerMeter();

        // Start sensor tracking
        startSensorTracking();

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            tvStatus.setText("CAMERA ERROR");
            Log.e("ARActivity", "Camera error: " + e.getMessage());
        }
    }

    private void loadAllCreatures() {
        dbManager.getAllCreatures(new DatabaseManager.Callback<List<CreatureEntity>>() {
            @Override
            public void onSuccess(List<CreatureEntity> creatures) {
                allCreatures = creatures;
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error loading creatures: " + error);
            }
        });
    }

    private void createColoredCircle(String creatureName) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        int color;
        switch(creatureName.toLowerCase()) {
            case "samodiva": color = 0xFFFFD966; break;
            case "zmey": color = 0xFFFF5722; break;
            case "karakondzul": color = 0xFF8D6E63; break;
            case "torbalan": color = 0xFF37474F; break;
            case "vila": color = 0xFFF06292; break;
            default: color = 0xFF4CAF50;
        }
        drawable.setColor(color);
        drawable.setStroke(4, Color.WHITE);
        drawable.setSize(200, 200);
        creatureSprite.setImageDrawable(drawable);
    }

    private void startSensorTracking() {
        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isThrowing || !spriteActive) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3);
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            float pitch = orientation[1];
            float roll = orientation[2];

            if (!calibrated && calibrationSamples < 30) {
                calibrationX += roll;
                calibrationY += pitch;
                calibrationSamples++;
                if (calibrationSamples == 30) {
                    calibrationX = calibrationX / 30;
                    calibrationY = calibrationY / 30;
                    calibrated = true;
                }
                return;
            }

            float adjustedRoll = roll - calibrationX;
            float adjustedPitch = pitch - calibrationY;

            if (containerWidth > 0 && containerHeight > 0 && spriteWidth > 0 && spriteHeight > 0) {
                float sensitivity = 400f;
                float targetX = (containerWidth / 2) + (adjustedRoll * sensitivity) - (spriteWidth / 2);
                float targetY = (containerHeight / 2) + (adjustedPitch * sensitivity) - (spriteHeight / 2);

                targetX = Math.max(0, Math.min(targetX, containerWidth - spriteWidth));
                targetY = Math.max(0, Math.min(targetY, containerHeight - spriteHeight));

                spriteX = spriteX * 0.85f + targetX * 0.15f;
                spriteY = spriteY * 0.85f + targetY * 0.15f;

                creatureSprite.setX(spriteX);
                creatureSprite.setY(spriteY);

                updateProximityAndScale();
            }
        }
    }

    private void updateProximityAndScale() {
        float centerX = (containerWidth - spriteWidth) / 2;
        float centerY = (containerHeight - spriteHeight) / 2;
        float distanceToCenter = (float) Math.sqrt(
                Math.pow(spriteX - centerX, 2) + Math.pow(spriteY - centerY, 2));
        float maxDistance = (float) Math.sqrt(Math.pow(containerWidth, 2) + Math.pow(containerHeight, 2)) / 2;
        float proximity = maxDistance > 0 ? 1 - (distanceToCenter / maxDistance) : 0;
        float minScale = 1.0f;
        float maxScale = 2.2f;
        float scale = minScale + (proximity * (maxScale - minScale));
        creatureSprite.setScaleX(scale);
        creatureSprite.setScaleY(scale);

        if (!isThrowing && canThrow && powerLevel < 70) {
            if (proximity > 0.7f) {
                tvStatus.setText("CREATURE CLOSE");
            } else if (proximity > 0.3f) {
                tvStatus.setText("GETTING CLOSER");
            } else {
                tvStatus.setText("MOVE YOUR PHONE");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void startPowerMeter() {
        powerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isThrowing && canThrow) {
                    if (powerIncreasing) {
                        powerLevel += 5;
                        if (powerLevel >= 100) {
                            powerLevel = 100;
                            powerIncreasing = false;
                        }
                    } else {
                        powerLevel -= 5;
                        if (powerLevel <= 0) {
                            powerLevel = 0;
                            powerIncreasing = true;
                        }
                    }

                    powerMeterText.setText(powerLevel + "%");

                    if (powerBarMaxWidth > 0) {
                        int targetWidth = (int)(powerBarMaxWidth * (powerLevel / 100f));
                        powerBar.getLayoutParams().width = targetWidth;
                        powerBar.requestLayout();
                    }

                    if (powerLevel >= 70 && powerLevel <= 90) {
                        powerBar.setBackgroundColor(Color.parseColor("#4CAF50"));
                        btnThrow.setBackgroundColor(Color.parseColor("#4CAF50"));
                        tvStatus.setText("OPTIMAL POWER");
                    } else if (powerLevel >= 50) {
                        powerBar.setBackgroundColor(Color.parseColor("#FFC107"));
                        btnThrow.setBackgroundColor(Color.parseColor("#FFC107"));
                        tvStatus.setText("POWER BUILDING");
                    } else {
                        powerBar.setBackgroundColor(Color.parseColor("#F44336"));
                        btnThrow.setBackgroundColor(Color.parseColor("#F44336"));
                        tvStatus.setText("TOO WEAK");
                    }

                    powerHandler.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void throwNet() {
        isThrowing = true;
        canThrow = false;
        powerHandler.removeCallbacksAndMessages(null);

        SoundManager.getInstance(this).playThrowSound();

        boolean isHit;
        final boolean wasPerfect;
        final int xpGained;
        final boolean isCritical;
        String resultText = "";

        if (powerLevel >= 70 && powerLevel <= 90) {
            isHit = true;
            wasPerfect = true;
            xpGained = currentCreature.getPointsValue();
            resultText = "PERFECT THROW";
            tvStatus.setText(resultText);
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));

            // Track perfect throw for achievements - FIXED
            AchievementManager am = AchievementManager.getInstance(ARActivity.this);
            am.updatePerfectThrow();

            // COMBO INCREASES ON PERFECT THROW
            currentCombo++;
            updateComboDisplay();
            checkComboMilestone();

            if (currentCombo == 3 || currentCombo == 5 || currentCombo == 10) {
                SoundManager.getInstance(ARActivity.this).playPerfectThrowSound();
            }

            if (currentCombo > bestCombo) {
                bestCombo = currentCombo;
            }

            SoundManager.getInstance(ARActivity.this).playPerfectThrowSound();

            if (Math.random() < 0.2) {
                isCritical = true;
                resultText = "CRITICAL CATCH!";
                tvStatus.setText(resultText);
            } else {
                isCritical = false;
            }
        }else if (powerLevel >= 50 && powerLevel <= 95) {
            isHit = true;
            wasPerfect = false;
            xpGained = currentCreature.getPointsValue() / 2;
            resultText = "GOOD THROW";
            tvStatus.setText(resultText);
            tvStatus.setTextColor(Color.parseColor("#FFC107"));

            // COMBO RESETS ON NORMAL THROW
            currentCombo = 0;
            updateComboDisplay();
            isCritical = false;
            AchievementManager am = AchievementManager.getInstance(this);
            am.resetCombo();
        } else {
            isHit = false;
            wasPerfect = false;
            xpGained = 0;
            resultText = "FAIL";
            tvStatus.setText(resultText);
            tvStatus.setTextColor(Color.parseColor("#F44336"));

            // COMBO RESETS ON FAIL
            currentCombo = 0;
            updateComboDisplay();
            isCritical = false;
            AchievementManager am = AchievementManager.getInstance(this);
            am.resetCombo();
            SoundManager.getInstance(this).playMissSound();
        }

        if (isHit) {
            int finalXp = isCritical ? xpGained * 2 : xpGained;
            tvStatus.setText(resultText + " +" + finalXp + " XP");
            tvStatus.setTextSize(16);
        } else {
            tvStatus.setText(resultText);
            tvStatus.setTextSize(16);
        }

        netImageView.setVisibility(View.VISIBLE);
        netImageView.setX(btnThrow.getX() + btnThrow.getWidth() / 2 - 30);
        netImageView.setY(btnThrow.getY() - 50);
        netImageView.setScaleX(0.5f);
        netImageView.setScaleY(0.5f);

        float creatureX = creatureSprite.getX() + creatureSprite.getWidth() / 2;
        float creatureY = creatureSprite.getY() + creatureSprite.getHeight() / 2;

        netImageView.animate()
                .x(creatureX - 30)
                .y(creatureY - 30)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .rotation(360)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        netImageView.animate()
                                .scaleX(1.5f)
                                .scaleY(1.5f)
                                .alpha(0f)
                                .setDuration(150)
                                .withEndAction(() -> {
                                    netImageView.setVisibility(View.GONE);
                                    netImageView.setAlpha(1f);
                                    netImageView.setScaleX(1f);
                                    netImageView.setScaleY(1f);

                                    if (isHit) {
                                        hitCount++;
                                        if (hitCount >= requiredHits) {
                                            catchSuccess(xpGained, wasPerfect, isCritical);
                                        } else {
                                            vibrationManager.tap();
                                            tvStatus.setText("HIT " + hitCount + "/" + requiredHits);
                                            tvStatus.setTextSize(16);
                                            tvStatus.setTextColor(Color.parseColor("#FFD700"));

                                            creatureSprite.animate()
                                                    .alpha(0.5f)
                                                    .setDuration(100)
                                                    .withEndAction(() -> creatureSprite.animate().alpha(1f).setDuration(100).start())
                                                    .start();

                                            new Handler().postDelayed(() -> {
                                                isThrowing = false;
                                                canThrow = true;
                                                tvStatus.setTextSize(14);
                                                tvStatus.setTextColor(Color.parseColor("#FFD700"));
                                                startPowerMeter();
                                            }, 500);
                                        }
                                    } else {
                                        currentCombo = 0;
                                        updateComboDisplay();

                                        creatureSprite.animate()
                                                .translationXBy(30)
                                                .setDuration(50)
                                                .withEndAction(() -> creatureSprite.animate()
                                                        .translationXBy(-60)
                                                        .setDuration(50)
                                                        .withEndAction(() -> creatureSprite.animate()
                                                                .translationXBy(30)
                                                                .setDuration(50)
                                                                .start())
                                                        .start())
                                                .start();

                                        new Handler().postDelayed(() -> {
                                            isThrowing = false;
                                            canThrow = true;
                                            tvStatus.setTextSize(14);
                                            tvStatus.setTextColor(Color.parseColor("#FFD700"));
                                            startPowerMeter();
                                        }, 800);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void updateComboDisplay() {
        if (tvCombo != null) {
            if (currentCombo > 0) {
                tvCombo.setVisibility(View.VISIBLE);
                tvCombo.setText("⚡ COMBO x" + currentCombo + " ⚡");
                tvCombo.setTextSize(24);
                tvCombo.setTextColor(Color.WHITE);
                tvCombo.setTypeface(null, Typeface.BOLD);
                tvCombo.setBackgroundColor(Color.parseColor("#FF0000"));
                tvCombo.setPadding(32, 20, 32, 20);
                tvCombo.bringToFront();
            } else {
                tvCombo.setVisibility(View.GONE);
            }
        }
    }

    private void checkComboMilestone() {
        int[] milestones = {3, 5, 10, 15, 20, 30, 50};
        for (int m : milestones) {
            if (currentCombo == m) {
                if (vibrationManager != null) {
                    vibrationManager.success();
                }
                String milestoneText;
                if (currentCombo >= 30) {
                    milestoneText = "🌟 GODLIKE COMBO x" + currentCombo + "! 🌟";
                } else if (currentCombo >= 20) {
                    milestoneText = "💥 UNSTOPPABLE COMBO x" + currentCombo + "! 💥";
                } else if (currentCombo >= 10) {
                    milestoneText = "🔥 EPIC COMBO x" + currentCombo + "! 🔥";
                } else {
                    milestoneText = "⚡ COMBO x" + currentCombo + "! ⚡";
                }
                tvStatus.setText(milestoneText);
                SoundManager.getInstance(this).playPerfectThrowSound();
                break;
            }
        }
    }

    private void checkLevelUp(int xpGained) {
        dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer totalXP) {
                int newLevel = 1 + (totalXP / 100);
                if (newLevel > currentLevel) {
                    currentLevel = newLevel;
                    vibrationManager.celebration();
                    SoundManager.getInstance(ARActivity.this).playLevelUpSound();
                    tvStatus.setText("LEVEL UP! Now Level " + currentLevel + "!");
                    tvStatus.setTextSize(18);
                    tvStatus.setTextColor(Color.parseColor("#FFD700"));

                    // Check achievement for level up
                    checkAchievements();
                }
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error checking level: " + error);
            }
        });
    }

    private void checkAchievements() {
        dbManager.getAllCreatures(new DatabaseManager.Callback<List<CreatureEntity>>() {
            @Override
            public void onSuccess(List<CreatureEntity> creatures) {
                final int totalCaptures;  // Make final
                final int discoveredSpecies;  // Make final
                int captures = 0;
                int species = 0;

                for (CreatureEntity creature : creatures) {
                    int count = creature.getCaptureCount();
                    captures += count;
                    if (count > 0) {
                        species++;
                    }
                }
                totalCaptures = captures;
                discoveredSpecies = species;

                dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
                    @Override
                    public void onSuccess(Integer totalXP) {
                        int level = 1 + (totalXP / 100);

                        AchievementManager am = AchievementManager.getInstance(ARActivity.this);
                        am.checkFirstCatch(totalCaptures);
                        am.checkLevelUp(level);
                        am.checkCollector(discoveredSpecies);
                        am.checkMasterHunter(discoveredSpecies, 15);
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("ARActivity", "Error getting XP: " + error);
                    }
                });
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error getting creatures: " + error);
            }
        });
    }

    private void catchSuccess(int xpGained, boolean wasPerfect, boolean isCritical) {
        int totalXP = xpGained;

        AchievementManager am = AchievementManager.getInstance(this);
        am.updateCombo(currentCombo);

        // Add combo bonus
        int comboBonus = (int)(totalXP * Math.min(1.0, currentCombo * 0.1));
        totalXP += comboBonus;

        // VIBRATION
        if (isCritical) {
            vibrationManager.celebration();
        } else if (wasPerfect) {
            vibrationManager.success();
        } else {
            vibrationManager.tap();
        }

        if (wasPerfect) {
            tvStatus.setText("CAPTURED! +" + totalXP + " XP (Combo: " + currentCombo + "x!)");
        } else {
            tvStatus.setText("CAPTURED! +" + totalXP + " XP");
        }

        // Critical catch bonus display
        if (isCritical) {
            tvStatus.setText("🔥 CRITICAL CATCH! +" + totalXP + " XP 🔥");
        }

        // Play catch sound
        SoundManager.getInstance(this).playCatchSound();

        // Update database with new capture count
        final int finalTotalXP = totalXP;
        dbManager.incrementCaptureCount(currentCreature.getName(), new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer newCount) {
                Log.d("ARActivity", "Capture count updated to: " + newCount);
                currentCreature.setCaptureCount(newCount);
                checkLevelUp(finalTotalXP);
                checkAchievements();
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error updating capture count: " + error);
            }
        });

        // Record for daily challenges
        dbManager.recordCapture(currentCreature.getName(), wasPerfect, new DatabaseManager.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("ARActivity", "Challenge progress recorded");
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error recording challenge: " + error);
            }
        });

        // Save XP
        dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer currentXP) {
                dbManager.saveXP(currentXP + finalTotalXP, new DatabaseManager.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d("ARActivity", "XP saved successfully");
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("ARActivity", "Error saving XP: " + error);
                    }
                });
            }
            @Override
            public void onError(String error) {
                Log.e("ARActivity", "Error getting XP: " + error);
            }
        });

        String message = "Captured " + currentCreature.getName() + "! +" + finalTotalXP + " XP";
        if (wasPerfect) message += " (Perfect! Combo: " + currentCombo + ")";
        if (isCritical) message += " CRITICAL!";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Return the updated creature data to MapsActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("captured_creature", currentCreature.getName());
        resultIntent.putExtra("xp_gained", finalTotalXP);
        resultIntent.putExtra("new_capture_count", currentCreature.getCaptureCount() + 1);
        resultIntent.putExtra("current_combo", currentCombo);
        setResult(RESULT_OK, resultIntent);

        // Catch animation
        creatureSprite.animate()
                .scaleX(2f).scaleY(2f)
                .alpha(0f)
                .setDuration(500)
                .start();

        new Handler().postDelayed(() -> finish(), 1000);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        spriteActive = false;
        powerHandler.removeCallbacksAndMessages(null);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        spriteActive = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && camera == null) {
            openCamera();
        }
        startSensorTracking();
        startPowerMeter();
    }
}