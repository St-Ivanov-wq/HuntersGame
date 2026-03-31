package com.example.huntersgame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnFindNearby;
    private Button btnBack;
    private TextView tvStatus;
    private TextView tvXP;

    private List<Creature> allCreatures;
    private List<Creature> uncapturedCreatures;
    private HashMap<Marker, Creature> creatureMarkers;
    private HashMap<Marker, LatLng> markerPositions;
    private Location currentLocation;
    private SaveManager saveManager;

    // XP System
    private int currentLevel = 1;
    private int currentXP = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnFindNearby = findViewById(R.id.btn_find_nearby);
        btnBack = findViewById(R.id.btn_back_maps);
        tvStatus = findViewById(R.id.tv_map_status);
        tvXP = findViewById(R.id.tv_xp);

        saveManager = new SaveManager(this);
        allCreatures = CreatureData.getCreatures();
        loadSavedData();

        loadXPData();
        updateXPDisplay();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        creatureMarkers = new HashMap<>();
        markerPositions = new HashMap<>();

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFindNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spawnNearbyCreatures();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadSavedData() {
        List<Creature> savedCreatures = saveManager.loadCreatures();
        if (savedCreatures != null && savedCreatures.size() == allCreatures.size()) {
            for (int i = 0; i < allCreatures.size(); i++) {
                allCreatures.get(i).setCaptured(savedCreatures.get(i).isCaptured());
            }
        }

        uncapturedCreatures = new ArrayList<>();
        for (Creature creature : allCreatures) {
            if (!creature.isCaptured()) {
                uncapturedCreatures.add(creature);
            }
        }
    }

    private void loadXPData() {
        currentXP = saveManager.loadPoints();
        currentLevel = 1 + (currentXP / 100);
    }

    private void updateXPDisplay() {
        int xpInCurrentLevel = currentXP % 100;
        tvXP.setText("⭐ LVL " + currentLevel + " (" + xpInCurrentLevel + "/100)");
    }

    private void addXP(int amount) {
        currentXP += amount;
        saveManager.savePoints(currentXP);

        int newLevel = 1 + (currentXP / 100);
        if (newLevel > currentLevel) {
            currentLevel = newLevel;
            tvStatus.setText("🎉 LEVEL UP! You are now Level " + currentLevel + " Hunter! 🎉");
        }

        updateXPDisplay();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener(this);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng currentLatLng = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    tvStatus.setText("📍 You are here! Tap Find Creatures to hunt!");
                } else {
                    tvStatus.setText("⚠️ Waiting for location...");
                }
            }
        });
    }

    private BitmapDescriptor getCustomMarker(String creatureName) {
        int color;
        switch(creatureName) {
            case "Samodiva": color = 0xFFFFD966; break;
            case "Zmey": color = 0xFFFF5722; break;
            case "Karakondzul": color = 0xFF8D6E63; break;
            case "Torbalan": color = 0xFF37474F; break;
            case "Vila": color = 0xFFF06292; break;
            default: color = 0xFF4CAF50;
        }

        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(40, 40, 35, paint);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(40, 40, 35, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        String letter = creatureName.substring(0, 1);
        canvas.drawText(letter, 40, 55, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void spawnNearbyCreatures() {
        if (currentLocation == null) {
            tvStatus.setText("⚠️ Getting your location first...");
            getCurrentLocation();
            return;
        }

        if (uncapturedCreatures.isEmpty()) {
            tvStatus.setText("🎉 You've captured all creatures! Amazing!");
            btnFindNearby.setEnabled(false);
            return;
        }

        mMap.clear();
        creatureMarkers.clear();
        markerPositions.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        int creatureCount = Math.min(5 + random.nextInt(3), uncapturedCreatures.size());

        for (int i = 0; i < creatureCount; i++) {
            int creatureIndex = random.nextInt(uncapturedCreatures.size());
            Creature creature = uncapturedCreatures.get(creatureIndex);

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 30 + random.nextDouble() * 70;
            double latOffset = (distance / 111320) * Math.cos(angle);
            double lngOffset = (distance / (111320 * Math.cos(currentLocation.getLatitude()))) * Math.sin(angle);

            LatLng creatureLocation = new LatLng(
                    currentLocation.getLatitude() + latOffset,
                    currentLocation.getLongitude() + lngOffset
            );

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(creatureLocation)
                    .title(creature.getName())
                    .snippet("⭐ " + creature.getPointsValue() + " XP | Tap to hunt in AR!")
                    .icon(getCustomMarker(creature.getName())));

            if (marker != null) {
                creatureMarkers.put(marker, creature);
                markerPositions.put(marker, creatureLocation);
            }
        }

        LatLng playerLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocation, 18));

        tvStatus.setText("🔍 " + creatureCount + " creatures nearby! Tap on them to hunt in AR!");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Creature creature = creatureMarkers.get(marker);
        if (creature != null && currentLocation != null) {
            LatLng creaturePos = marker.getPosition();

            // Calculate distance
            Location creatureLocation = new Location("");
            creatureLocation.setLatitude(creaturePos.latitude);
            creatureLocation.setLongitude(creaturePos.longitude);

            float distanceMeters = currentLocation.distanceTo(creatureLocation);
            int distanceRounded = Math.round(distanceMeters);

            if (distanceRounded <= 100) {
                // Within range - Open AR mode to catch!
                tvStatus.setText("🎯 Entering AR mode to catch " + creature.getName() + "!");

                // Pass creature to AR activity
                Intent intent = new Intent(MapsActivity.this, ARActivity.class);
                intent.putExtra("creature_name", creature.getName());
                intent.putExtra("creature_xp", creature.getPointsValue());
                startActivityForResult(intent, 1);
            } else {
                // Too far
                tvStatus.setText("❌ Too far! You're " + distanceRounded + "m away. Walk closer! (Need < 100m)");
                Toast.makeText(this, "Walk closer to " + distanceRounded + "m away!", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Creature was captured in AR mode
            if (data != null) {
                String creatureName = data.getStringExtra("captured_creature");
                int xpGained = data.getIntExtra("xp_gained", 0);

                // Update creature as captured
                for (Creature creature : allCreatures) {
                    if (creature.getName().equals(creatureName)) {
                        creature.setCaptured(true);
                        break;
                    }
                }

                saveManager.saveCreatures(allCreatures);
                addXP(xpGained);
                loadSavedData(); // Refresh list

                // Remove marker from map
                for (Marker marker : creatureMarkers.keySet()) {
                    Creature c = creatureMarkers.get(marker);
                    if (c != null && c.getName().equals(creatureName)) {
                        marker.remove();
                        creatureMarkers.remove(marker);
                        break;
                    }
                }

                tvStatus.setText("✅ Captured " + creatureName + "! +" + xpGained + " XP!");

                if (uncapturedCreatures.isEmpty()) {
                    tvStatus.setText("🎉 CONGRATULATIONS! Master Hunter! 🎉");
                    btnFindNearby.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                    }
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission needed to find creatures!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh creatures list
        loadSavedData();
        if (currentLocation != null) {
            spawnNearbyCreatures();
        }
    }
}