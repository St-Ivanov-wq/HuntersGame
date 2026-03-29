package com.example.huntersgame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ARActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {

    private Button btnSpawnCreature;
    private TextView tvStatus;
    private Button btnCapture;
    private Button btnBack;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private ImageView creatureSprite;
    private CardView infoCard;
    private TextView creatureName;
    private TextView creatureDescription;
    private TextView creaturePoints;
    private FrameLayout spriteContainer;

    private Creature currentCreature;
    private SaveManager saveManager;
    private List<Creature> uncapturedCreatures;
    private List<Creature> allCreatures;

    // AR Movement variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    // Calibration values
    private float calibrationX = 0;
    private float calibrationY = 0;
    private boolean calibrated = false;
    private int calibrationSamples = 0;

    private float spriteX = 0;
    private float spriteY = 0;
    private int containerWidth = 0;
    private int containerHeight = 0;
    private int spriteWidth = 0;
    private int spriteHeight = 0;
    private boolean spriteActive = false;
    private boolean useRandomMovement = false;

    private Handler handler = new Handler();
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_simple);

        surfaceView = findViewById(R.id.surface_view);
        btnSpawnCreature = findViewById(R.id.btn_spawn_creature);
        tvStatus = findViewById(R.id.tv_ar_status);
        btnCapture = findViewById(R.id.btn_ar_capture);
        btnBack = findViewById(R.id.btn_ar_back);
        creatureSprite = findViewById(R.id.creature_sprite);
        infoCard = findViewById(R.id.info_card);
        creatureName = findViewById(R.id.creature_name);
        creatureDescription = findViewById(R.id.creature_description);
        creaturePoints = findViewById(R.id.creature_points);
        spriteContainer = findViewById(R.id.sprite_container);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        saveManager = new SaveManager(this);
        allCreatures = CreatureData.getCreatures();
        loadUncapturedCreatures();

        // Initialize sensors for AR movement
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        btnSpawnCreature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spawnCreature();
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

        btnCapture.setEnabled(false);

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
            tvStatus.setText("📷 Move your phone to find the creature!");
        } catch (Exception e) {
            tvStatus.setText("⚠️ Camera error: " + e.getMessage());
        }
    }

    private void loadUncapturedCreatures() {
        List<Creature> savedCreatures = saveManager.loadCreatures();
        if (savedCreatures != null && savedCreatures.size() == allCreatures.size()) {
            for (int i = 0; i < allCreatures.size(); i++) {
                allCreatures.get(i).setCaptured(savedCreatures.get(i).isCaptured());
            }
        }

        uncapturedCreatures = new java.util.ArrayList<>();
        for (Creature creature : allCreatures) {
            if (!creature.isCaptured()) {
                uncapturedCreatures.add(creature);
            }
        }
    }

    private void spawnCreature() {
        if (uncapturedCreatures.isEmpty()) {
            tvStatus.setText("🎉 You've captured all creatures!");
            btnSpawnCreature.setEnabled(false);
            return;
        }

        Random random = new Random();
        int index = random.nextInt(uncapturedCreatures.size());
        currentCreature = uncapturedCreatures.get(index);

        // Set creature sprite image
        setCreatureImage(currentCreature.getName());

        // Show sprite
        creatureSprite.setVisibility(View.VISIBLE);
        infoCard.setVisibility(View.VISIBLE);
        spriteActive = true;

        // Reset calibration
        calibrated = false;
        calibrationSamples = 0;
        calibrationX = 0;
        calibrationY = 0;

        // Get container dimensions
        spriteContainer.post(new Runnable() {
            @Override
            public void run() {
                containerWidth = spriteContainer.getWidth();
                containerHeight = spriteContainer.getHeight();
                spriteWidth = creatureSprite.getWidth();
                spriteHeight = creatureSprite.getHeight();

                if (containerWidth > 0 && containerHeight > 0 && spriteWidth > 0 && spriteHeight > 0) {
                    // Center the sprite
                    spriteX = (containerWidth - spriteWidth) / 2;
                    spriteY = (containerHeight - spriteHeight) / 2;
                    creatureSprite.setX(spriteX);
                    creatureSprite.setY(spriteY);
                }
            }
        });

        // Update info card
        creatureName.setText(currentCreature.getName() + " (" + currentCreature.getNameBulgarian() + ")");
        creatureDescription.setText(currentCreature.getDescription());
        creaturePoints.setText("⭐ " + currentCreature.getPointsValue() + " points");

        tvStatus.setText("🔍 " + currentCreature.getName() + " is nearby! Move your phone to find it!");
        btnCapture.setEnabled(true);

        // Start sensor tracking
        startSensorTracking();
    }

    private void setCreatureImage(String creatureName) {
        int imageResId;
        switch(creatureName) {
            case "Samodiva":
                imageResId = getResources().getIdentifier("samodiva", "drawable", getPackageName());
                if (imageResId == 0) imageResId = android.R.drawable.ic_menu_camera;
                break;
            case "Zmey":
                imageResId = getResources().getIdentifier("zmey", "drawable", getPackageName());
                if (imageResId == 0) imageResId = android.R.drawable.ic_menu_compass;
                break;
            case "Karakondzul":
                imageResId = getResources().getIdentifier("karakondzul", "drawable", getPackageName());
                if (imageResId == 0) imageResId = android.R.drawable.ic_menu_delete;
                break;
            case "Torbalan":
                imageResId = getResources().getIdentifier("torbalan", "drawable", getPackageName());
                if (imageResId == 0) imageResId = android.R.drawable.ic_menu_edit;
                break;
            case "Vila":
                imageResId = getResources().getIdentifier("vila", "drawable", getPackageName());
                if (imageResId == 0) imageResId = android.R.drawable.ic_menu_gallery;
                break;
            default:
                imageResId = android.R.drawable.ic_menu_info_details;
        }

        creatureSprite.setImageResource(imageResId);
    }

    private void startSensorTracking() {
        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            tvStatus.setText("🔍 " + currentCreature.getName() + " is nearby! Move your phone to find it!");
        } else {
            useRandomMovement = true;
            startRandomMovement();
        }
    }

    private void startRandomMovement() {
        tvStatus.setText("✨ " + currentCreature.getName() + " is moving around! Chase it!");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (spriteActive && currentCreature != null) {
                    randomMoveSprite();
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void randomMoveSprite() {
        if (containerWidth == 0 || containerHeight == 0) return;

        Random random = new Random();
        float newX = spriteX + (random.nextFloat() - 0.5f) * 20;
        float newY = spriteY + (random.nextFloat() - 0.5f) * 20;

        // Keep within bounds
        newX = Math.max(0, Math.min(newX, containerWidth - spriteWidth));
        newY = Math.max(0, Math.min(newY, containerHeight - spriteHeight));

        spriteX = newX;
        spriteY = newY;

        creatureSprite.animate().x(spriteX).y(spriteY).setDuration(100).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!spriteActive || currentCreature == null || useRandomMovement) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3);
        }

        // Calculate rotation
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Use orientation to move sprite
            float pitch = orientation[1];  // Up/down tilt (range: -1.5 to 1.5)
            float roll = orientation[2];   // Left/right tilt (range: -1.5 to 1.5)

            // Calibrate after a few samples
            if (!calibrated && calibrationSamples < 30) {
                calibrationX += roll;
                calibrationY += pitch;
                calibrationSamples++;

                if (calibrationSamples == 30) {
                    calibrationX = calibrationX / 30;
                    calibrationY = calibrationY / 30;
                    calibrated = true;
                    tvStatus.setText("🔍 Calibrated! Move your phone to find the creature!");
                }
                return;
            }

            // Apply calibration
            float adjustedRoll = roll - calibrationX;
            float adjustedPitch = pitch - calibrationY;

            if (containerWidth > 0 && containerHeight > 0 && spriteWidth > 0 && spriteHeight > 0) {
                // Map sensor values to screen movement (reduced sensitivity)
                float sensitivity = 200f;
                float targetX = (containerWidth / 2) + (adjustedRoll * sensitivity) - (spriteWidth / 2);
                float targetY = (containerHeight / 2) + (adjustedPitch * sensitivity) - (spriteHeight / 2);

                // Keep within bounds
                targetX = Math.max(0, Math.min(targetX, containerWidth - spriteWidth));
                targetY = Math.max(0, Math.min(targetY, containerHeight - spriteHeight));

                // Smooth movement
                spriteX = spriteX * 0.85f + targetX * 0.15f;
                spriteY = spriteY * 0.85f + targetY * 0.15f;

                creatureSprite.setX(spriteX);
                creatureSprite.setY(spriteY);

                // Update proximity feedback
                updateProximityFeedback();
            }
        }
    }

    private void updateProximityFeedback() {
        float centerX = (containerWidth - spriteWidth) / 2;
        float centerY = (containerHeight - spriteHeight) / 2;
        float distanceToCenter = (float) Math.sqrt(
                Math.pow(spriteX - centerX, 2) +
                        Math.pow(spriteY - centerY, 2)
        );

        float maxDistance = (float) Math.sqrt(Math.pow(containerWidth, 2) + Math.pow(containerHeight, 2)) / 2;
        float proximity = 1 - (distanceToCenter / maxDistance);

        if (proximity > 0.7f) {
            tvStatus.setText("⚠️ " + currentCreature.getName() + " is RIGHT IN FRONT OF YOU! Capture it!");
            // Make sprite pulse when close
            creatureSprite.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (spriteActive) {
                        creatureSprite.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    }
                }
            }).start();
        } else if (proximity > 0.4f) {
            tvStatus.setText("🔍 " + currentCreature.getName() + " is getting closer...");
        } else {
            tvStatus.setText("📍 " + currentCreature.getName() + " is out there. Move your phone!");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void captureCreature() {
        if (currentCreature != null && !currentCreature.isCaptured()) {
            spriteActive = false;
            useRandomMovement = false;

            // Stop sensors
            sensorManager.unregisterListener(this);

            currentCreature.setCaptured(true);

            saveManager.saveCreatures(allCreatures);
            int currentPoints = saveManager.loadPoints();
            saveManager.savePoints(currentPoints + currentCreature.getPointsValue());

            uncapturedCreatures.remove(currentCreature);

            // Hide sprite and card with capture effect
            creatureSprite.animate()
                    .scaleX(2f).scaleY(2f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            creatureSprite.setVisibility(View.GONE);
                            creatureSprite.setScaleX(1f);
                            creatureSprite.setScaleY(1f);
                            creatureSprite.setAlpha(1f);
                            infoCard.setVisibility(View.GONE);
                        }
                    });

            Toast.makeText(this, "✅ Captured " + currentCreature.getName() + "! +" +
                    currentCreature.getPointsValue() + " points!", Toast.LENGTH_LONG).show();

            tvStatus.setText("✅ Captured! " + uncapturedCreatures.size() + " creatures left!");

            btnCapture.setEnabled(false);
            currentCreature = null;

            if (uncapturedCreatures.isEmpty()) {
                tvStatus.setText("🎉 CONGRATULATIONS! You've captured ALL mythical creatures! 🎉");
                btnSpawnCreature.setEnabled(false);
            } else {
                tvStatus.setText("🔍 Tap 'Find Creature' to find the next one!");
            }
        }
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
                Toast.makeText(this, "Camera permission needed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        spriteActive = false;
        useRandomMovement = false;
        sensorManager.unregisterListener(this);
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && camera == null) {
            openCamera();
        }
    }
}