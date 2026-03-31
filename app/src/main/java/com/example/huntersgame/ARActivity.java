package com.example.huntersgame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;

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

    private Creature currentCreature;
    private SaveManager saveManager;

    private static final int CAMERA_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        saveManager = new SaveManager(this);

        // Get creature from intent
        Intent intent = getIntent();
        String creatureName = intent.getStringExtra("creature_name");
        int creatureXP = intent.getIntExtra("creature_xp", 0);

        // Create creature object
        currentCreature = new Creature("temp", creatureName, "", "", "", creatureXP, 0);

        // Set up UI
        tvCreatureName.setText(creatureName);
        tvCreaturePoints.setText("⭐ " + creatureXP + " XP");

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

        tvStatus.setText("📷 Move your phone to find " + creatureName + "!");

        // Set up throw button
        btnThrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canThrow && !isThrowing) {
                    throwNet();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // Get power bar max width
        powerBar.post(new Runnable() {
            @Override
            public void run() {
                powerBarMaxWidth = powerBar.getWidth();
            }
        });

        // Get sprite container dimensions
        spriteContainer.post(new Runnable() {
            @Override
            public void run() {
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
                Math.pow(spriteX - centerX, 2) +
                        Math.pow(spriteY - centerY, 2)
        );

        float maxDistance = (float) Math.sqrt(Math.pow(containerWidth, 2) + Math.pow(containerHeight, 2)) / 2;
        float proximity = maxDistance > 0 ? 1 - (distanceToCenter / maxDistance) : 0;

        float minScale = 1.0f;
        float maxScale = 2.2f;
        float scale = minScale + (proximity * (maxScale - minScale));
        creatureSprite.setScaleX(scale);
        creatureSprite.setScaleY(scale);

        if (proximity > 0.7f) {
            tvStatus.setText("⚠️ " + currentCreature.getName() + " is CLOSE! Throw when power is GREEN!");
        } else if (proximity > 0.3f) {
            tvStatus.setText("🔍 Getting closer! Keep moving your phone...");
        } else {
            tvStatus.setText("📍 " + currentCreature.getName() + " is out there. Move your phone!");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void startPowerMeter() {
        powerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isThrowing && canThrow) {
                    // Update power level
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

                    // Update percentage text
                    powerMeterText.setText(powerLevel + "%");

                    // Update power bar width (moving bar)
                    if (powerBarMaxWidth > 0) {
                        int targetWidth = (int)(powerBarMaxWidth * (powerLevel / 100f));
                        powerBar.getLayoutParams().width = targetWidth;
                        powerBar.requestLayout();
                    }

                    // Change power bar COLOR based on zone
                    if (powerLevel >= 70 && powerLevel <= 90) {
                        powerBar.setBackgroundColor(Color.parseColor("#4CAF50"));
                        btnThrow.setBackgroundColor(Color.parseColor("#4CAF50"));
                        tvStatus.setText("🔥 PERFECT! THROW NOW!");
                    } else if (powerLevel >= 50) {
                        powerBar.setBackgroundColor(Color.parseColor("#FFC107"));
                        btnThrow.setBackgroundColor(Color.parseColor("#FFC107"));
                        tvStatus.setText("👍 Getting there! Wait for GREEN!");
                    } else {
                        powerBar.setBackgroundColor(Color.parseColor("#F44336"));
                        btnThrow.setBackgroundColor(Color.parseColor("#F44336"));
                        tvStatus.setText("⚠️ Too weak! Wait for power to build!");
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

        boolean isHit;

        if (powerLevel >= 70 && powerLevel <= 90) {
            isHit = true;
            tvStatus.setText("🔥 PERFECT HIT!");
        } else if (powerLevel >= 50 && powerLevel <= 95) {
            isHit = true;
            tvStatus.setText("✨ GOOD HIT!");
        } else {
            isHit = false;
            tvStatus.setText("❌ MISS! Power: " + powerLevel + "%");
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
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        netImageView.setVisibility(View.GONE);
                                        netImageView.setAlpha(1f);
                                        netImageView.setScaleX(1f);
                                        netImageView.setScaleY(1f);

                                        if (isHit) {
                                            hitCount++;

                                            if (hitCount >= requiredHits) {
                                                catchSuccess();
                                            } else {
                                                tvStatus.setText("🎯 HIT! " + hitCount + "/" + requiredHits + " hits!");

                                                creatureSprite.animate()
                                                        .alpha(0.5f)
                                                        .setDuration(100)
                                                        .withEndAction(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                creatureSprite.animate().alpha(1f).setDuration(100).start();
                                                            }
                                                        })
                                                        .start();

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        isThrowing = false;
                                                        canThrow = true;
                                                        startPowerMeter();
                                                    }
                                                }, 500);
                                            }
                                        } else {
                                            creatureSprite.animate()
                                                    .translationXBy(30)
                                                    .setDuration(50)
                                                    .withEndAction(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            creatureSprite.animate()
                                                                    .translationXBy(-60)
                                                                    .setDuration(50)
                                                                    .withEndAction(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            creatureSprite.animate()
                                                                                    .translationXBy(30)
                                                                                    .setDuration(50)
                                                                                    .start();
                                                                        }
                                                                    })
                                                                    .start();
                                                        }
                                                    })
                                                    .start();

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    isThrowing = false;
                                                    canThrow = true;
                                                    startPowerMeter();
                                                }
                                            }, 800);
                                        }
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void catchSuccess() {
        int totalXP = currentCreature.getPointsValue();

        tvStatus.setText("✅ CAUGHT! +" + totalXP + " XP!");
        Toast.makeText(this, "You caught " + currentCreature.getName() + "! +" + totalXP + " XP!", Toast.LENGTH_LONG).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("captured_creature", currentCreature.getName());
        resultIntent.putExtra("xp_gained", totalXP);
        setResult(RESULT_OK, resultIntent);

        creatureSprite.animate()
                .scaleX(2f).scaleY(2f)
                .alpha(0f)
                .setDuration(500)
                .start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void openCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            tvStatus.setText("⚠️ Camera error");
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