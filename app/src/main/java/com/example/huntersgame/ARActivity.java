package com.example.huntersgame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ARActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Button btnSpawnCreature;
    private TextView tvStatus;
    private Button btnCapture;
    private Button btnBack;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private Creature currentCreature;
    private SaveManager saveManager;
    private List<Creature> uncapturedCreatures;
    private List<Creature> allCreatures;

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

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        saveManager = new SaveManager(this);
        allCreatures = CreatureData.getCreatures();
        loadUncapturedCreatures();

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
            tvStatus.setText("📷 Camera ready! Tap 'Spawn Creature' to hunt!");
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

        tvStatus.setText("✨ " + currentCreature.getName() + " found! Look through your camera!");
        btnCapture.setEnabled(true);

        showCreatureInfo();
    }

    private void showCreatureInfo() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(currentCreature.getName() + " (" + currentCreature.getNameBulgarian() + ")");
        builder.setMessage(
                "📖 " + currentCreature.getDescription() + "\n\n" +
                        "🏞️ Habitat: " + currentCreature.getHabitat() + "\n" +
                        "⭐ Worth: " + currentCreature.getPointsValue() + " points\n\n" +
                        "Look through your camera! This creature is watching you!\n\n" +
                        "Tap 'Capture!' to add it to your collection!"
        );
        builder.setPositiveButton("Got it!", null);
        builder.show();
    }

    private void captureCreature() {
        if (currentCreature != null && !currentCreature.isCaptured()) {
            currentCreature.setCaptured(true);

            saveManager.saveCreatures(allCreatures);
            int currentPoints = saveManager.loadPoints();
            saveManager.savePoints(currentPoints + currentCreature.getPointsValue());

            uncapturedCreatures.remove(currentCreature);

            Toast.makeText(this, "✅ Captured " + currentCreature.getName() + "! +" +
                    currentCreature.getPointsValue() + " points!", Toast.LENGTH_LONG).show();

            tvStatus.setText("✅ Captured! " + uncapturedCreatures.size() + " creatures left!");

            btnCapture.setEnabled(false);
            currentCreature = null;

            if (uncapturedCreatures.isEmpty()) {
                tvStatus.setText("🎉 CONGRATULATIONS! You've captured ALL mythical creatures! 🎉");
                btnSpawnCreature.setEnabled(false);
            } else {
                tvStatus.setText("🔍 Tap 'Spawn Creature' to find the next one!");
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