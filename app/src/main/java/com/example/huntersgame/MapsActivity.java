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
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Button btnFindNearby;
    private Button btnBack;
    private TextView tvStatus;
    private TextView tvXP;

    private List<Creature> allCreatures;
    private HashMap<Marker, Creature> creatureMarkers;
    private Location currentLocation;
    private SaveManager saveManager;

    // XP System
    private int currentLevel = 1;
    private int currentXP = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private Random random = new Random();
    private boolean creaturesSpawned = false;

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

        setupLocationCallback();

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
                stopLocationUpdates();
                finish();
            }
        });
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    private void updateLocation(Location location) {
        if (location != null) {
            currentLocation = location;

            if (mMap != null && !creaturesSpawned) {
                LatLng currentLatLng = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                spawnNearbyCreatures();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void loadSavedData() {
        List<Creature> savedCreatures = saveManager.loadCreatures();
        if (savedCreatures != null && savedCreatures.size() == allCreatures.size()) {
            for (int i = 0; i < allCreatures.size(); i++) {
                allCreatures.get(i).setCaptureCount(savedCreatures.get(i).getCaptureCount());
                allCreatures.get(i).setCaptured(savedCreatures.get(i).isCaptured());
            }
        }

        // Debug: print loaded capture counts
        for (Creature creature : allCreatures) {
            android.util.Log.d("MapsActivity", "LOADED - " + creature.getName() + " capture count: " + creature.getCaptureCount());
        }
    }

    private void saveAllData() {
        saveManager.saveCreatures(allCreatures);

        // Debug: print saved capture counts
        for (Creature creature : allCreatures) {
            android.util.Log.d("MapsActivity", "SAVED - " + creature.getName() + " capture count: " + creature.getCaptureCount());
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
            tvStatus.setText("LEVEL UP! You are now Level " + currentLevel + " Hunter!");
        }

        updateXPDisplay();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMarkerClickListener(this);
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
            tvStatus.setText("Getting your location...");
            return;
        }

        // Clear old markers
        mMap.clear();
        creatureMarkers.clear();

        // Re-add player location marker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Spawn 5-8 creatures
        int creatureCount = 5 + random.nextInt(4);

        List<String> spawnedThisBatch = new ArrayList<>();

        for (int i = 0; i < creatureCount; i++) {
            int creatureIndex = random.nextInt(allCreatures.size());
            Creature creature = allCreatures.get(creatureIndex);

            // Avoid spawning same creature twice in one batch
            while (spawnedThisBatch.contains(creature.getName()) && spawnedThisBatch.size() < allCreatures.size()) {
                creatureIndex = random.nextInt(allCreatures.size());
                creature = allCreatures.get(creatureIndex);
            }
            spawnedThisBatch.add(creature.getName());

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 50 + random.nextDouble() * 200;
            double latOffset = (distance / 111320) * Math.cos(angle);
            double lngOffset = (distance / (111320 * Math.cos(currentLocation.getLatitude()))) * Math.sin(angle);

            LatLng creatureLocation = new LatLng(
                    currentLocation.getLatitude() + latOffset,
                    currentLocation.getLongitude() + lngOffset
            );

            int captureCount = creature.getCaptureCount();
            String snippet = creature.getPointsValue() + " XP | Caught: " + captureCount + "x";

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(creatureLocation)
                    .title(creature.getName())
                    .snippet(snippet)
                    .icon(getCustomMarker(creature.getName())));

            if (marker != null) {
                creatureMarkers.put(marker, creature);
            }
        }

        LatLng playerLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocation, 16));

        creaturesSpawned = true;
        tvStatus.setText(creatureCount + " creatures nearby! Walk to them and tap to hunt!");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Creature creature = creatureMarkers.get(marker);
        if (creature != null && currentLocation != null) {
            LatLng creaturePos = marker.getPosition();

            Location creatureLocation = new Location("");
            creatureLocation.setLatitude(creaturePos.latitude);
            creatureLocation.setLongitude(creaturePos.longitude);

            float distanceMeters = currentLocation.distanceTo(creatureLocation);
            int distanceRounded = Math.round(distanceMeters);

            if (distanceRounded <= 150) {
                tvStatus.setText("Entering AR mode to catch " + creature.getName() + "!");

                Intent intent = new Intent(MapsActivity.this, ARActivity.class);
                intent.putExtra("creature_name", creature.getName());
                intent.putExtra("creature_xp", creature.getPointsValue());
                startActivityForResult(intent, 1);
            } else {
                tvStatus.setText("Too far! You're " + distanceRounded + "m away. Walk closer!");
                Toast.makeText(this, "Walk closer! " + distanceRounded + "m away!", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String creatureName = data.getStringExtra("captured_creature");
                int newCaptureCount = data.getIntExtra("new_capture_count", 0);
                int xpGained = data.getIntExtra("xp_gained", 0);

                // Update the creature in our list
                for (Creature creature : allCreatures) {
                    if (creature.getName().equals(creatureName)) {
                        creature.setCaptureCount(newCaptureCount);
                        creature.setCaptured(true);
                        break;
                    }
                }

                // Save everything immediately
                saveAllData();

                // Update XP
                addXP(xpGained);

                // Remove the captured marker from map
                Marker toRemove = null;
                for (Map.Entry<Marker, Creature> entry : creatureMarkers.entrySet()) {
                    if (entry.getValue().getName().equals(creatureName)) {
                        toRemove = entry.getKey();
                        break;
                    }
                }
                if (toRemove != null) {
                    toRemove.remove();
                    creatureMarkers.remove(toRemove);
                }

                tvStatus.setText("Captured " + creatureName + "! (Now " + newCaptureCount + " total) +" + xpGained + " XP!");

                // Update snippets for remaining markers
                for (Map.Entry<Marker, Creature> entry : creatureMarkers.entrySet()) {
                    Creature c = entry.getValue();
                    // Get the latest capture count from allCreatures
                    for (Creature creature : allCreatures) {
                        if (creature.getName().equals(c.getName())) {
                            c.setCaptureCount(creature.getCaptureCount());
                            break;
                        }
                    }
                    String newSnippet = c.getPointsValue() + " XP | Caught: " + c.getCaptureCount() + "x";
                    entry.getKey().setSnippet(newSnippet);
                }

                if (creatureMarkers.size() > 0) {
                    tvStatus.setText(creatureMarkers.size() + " creatures remaining!");
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
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(this, "Location permission needed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedData();
        if (currentLocation != null && !creaturesSpawned) {
            spawnNearbyCreatures();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}