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
import android.util.Log;
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

    private List<CreatureEntity> allCreatures;
    private HashMap<Marker, CreatureEntity> creatureMarkers;
    private Location currentLocation;
    private DatabaseManager dbManager;

    // XP System
    private int currentLevel = 1;
    private int currentXP = 0;

    // Combo System
    private int currentCombo = 0;
    private int bestCombo = 0;

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

        dbManager = new DatabaseManager(this);
        allCreatures = new ArrayList<>();
        creatureMarkers = new HashMap<>();

        loadXPData();
        updateXPDisplay();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLocationCallback();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFindNearby.setOnClickListener(v -> spawnNearbyCreatures());
        btnBack.setOnClickListener(v -> {
            stopLocationUpdates();
            finish();
        });

        loadCreaturesFromDatabase();
    }

    private void loadCreaturesFromDatabase() {
        dbManager.getAllCreatures(new DatabaseManager.Callback<List<CreatureEntity>>() {
            @Override
            public void onSuccess(List<CreatureEntity> creatures) {
                allCreatures = creatures;
                if (currentLocation != null && !creaturesSpawned) {
                    spawnNearbyCreatures();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("MapsActivity", "Error loading creatures: " + error);
                Toast.makeText(MapsActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAllData() {
        dbManager.saveCreatures(allCreatures);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    private void updateLocation(Location location) {
        if (location != null) {
            currentLocation = location;
            if (mMap != null && !creaturesSpawned && !allCreatures.isEmpty()) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
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

    private void loadXPData() {
        dbManager.getTotalXP(new DatabaseManager.Callback<Integer>() {
            @Override
            public void onSuccess(Integer xp) {
                currentXP = xp;
                currentLevel = 1 + (currentXP / 100);
                updateXPDisplay();
            }

            @Override
            public void onError(String error) {
                currentXP = 0;
                updateXPDisplay();
            }
        });
    }

    private void updateXPDisplay() {
        int xpInCurrentLevel = currentXP % 100;
        tvXP.setText("⭐ LVL " + currentLevel + " (" + xpInCurrentLevel + "/100)");
    }

    private void addXP(int amount) {
        currentXP += amount;
        dbManager.saveXP(currentXP, new DatabaseManager.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                int newLevel = 1 + (currentXP / 100);
                if (newLevel > currentLevel) {
                    currentLevel = newLevel;
                    tvStatus.setText("LEVEL UP! You are now Level " + currentLevel + " Hunter!");
                }
                updateXPDisplay();
            }

            @Override
            public void onError(String error) {
                Log.e("MapsActivity", "Error saving XP: " + error);
            }
        });
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
        switch(creatureName.toLowerCase()) {
            case "samodiva":
                color = 0xFFFFD966;  // Golden yellow
                break;
            case "zmey":
                color = 0xFFFF5722;  // Deep orange
                break;
            case "karakondzul":
                color = 0xFF8D6E63;  // Brown
                break;
            case "torbalan":
                color = 0xFF37474F;  // Dark gray-blue
                break;
            case "vila":
                color = 0xFFF06292;  // Pink
                break;
            case "poludnitsa":
                color = 0xFFFFA726;  // Orange
                break;
            case "talasam":
                color = 0xFF42A5F5;  // Bright blue
                break;
            case "lamia":
                color = 0xFFEF5350;  // Red
                break;
            case "baba yaga":
                color = 0xFFAB47BC;  // Purple
                break;
            case "orisnizi":
                color = 0xFFEC407A;  // Hot pink
                break;
            case "hala":
                color = 0xFF26C6DA;  // Cyan
                break;
            case "borislav":
                color = 0xFF66BB6A;  // Forest green
                break;
            case "chuma":
                color = 0xFF7E57C2;  // Deep purple
                break;
            case "drekavac":
                color = 0xFFFF7043;  // Coral
                break;
            case "rusalka":
                color = 0xFF29B6F6;  // Light blue
                break;
            default:
                color = 0xFF4CAF50;  // Default green
                break;
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

        // Get first letter (handle multi-word names like "Baba Yaga")
        String letter = creatureName.substring(0, 1).toUpperCase();
        canvas.drawText(letter, 40, 55, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void spawnNearbyCreatures() {
        if (currentLocation == null || allCreatures.isEmpty()) return;

        mMap.clear();
        creatureMarkers.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        int creatureCount = Math.min(5 + random.nextInt(4), allCreatures.size());
        List<String> spawnedThisBatch = new ArrayList<>();

        for (int i = 0; i < creatureCount; i++) {
            int creatureIndex = random.nextInt(allCreatures.size());
            CreatureEntity creature = allCreatures.get(creatureIndex);
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

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(creatureLocation)
                    .title(creature.getName())
                    .snippet(creature.getPointsValue() + " XP | Caught: " + creature.getCaptureCount() + "x")
                    .icon(getCustomMarker(creature.getName())));

            if (marker != null) creatureMarkers.put(marker, creature);
        }

        LatLng playerLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocation, 16));
        creaturesSpawned = true;
        tvStatus.setText(creatureCount + " creatures nearby! Tap to hunt!");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        CreatureEntity creature = creatureMarkers.get(marker);
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
                intent.putExtra("current_combo", currentCombo);  // Pass current combo
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
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String creatureName = data.getStringExtra("captured_creature");
            int xpGained = data.getIntExtra("xp_gained", 0);
            int newCombo = data.getIntExtra("current_combo", 0);  // Get combo back

            // Update combo
            currentCombo = newCombo;
            if (currentCombo > bestCombo) {
                bestCombo = currentCombo;
            }

            // Update the creature in our list as captured
            for (CreatureEntity creature : allCreatures) {
                if (creature.getName().equals(creatureName)) {
                    int newCount = creature.getCaptureCount() + 1;
                    creature.setCaptureCount(newCount);
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
            for (Map.Entry<Marker, CreatureEntity> entry : creatureMarkers.entrySet()) {
                if (entry.getValue().getName().equals(creatureName)) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            if (toRemove != null) {
                toRemove.remove();
                creatureMarkers.remove(toRemove);
            }

            tvStatus.setText("Captured " + creatureName + "! +" + xpGained + " XP! Combo: " + currentCombo);
            if (creatureMarkers.size() > 0) {
                tvStatus.setText(creatureMarkers.size() + " creatures remaining!");
            }
        } else if (requestCode == 1 && resultCode == RESULT_CANCELED && data != null) {
            // Catch failed, reset combo
            currentCombo = 0;
            tvStatus.setText("Missed! Combo reset.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCreaturesFromDatabase();
        loadXPData();
        if (currentLocation != null && !creaturesSpawned && !allCreatures.isEmpty()) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) mMap.setMyLocationEnabled(true);
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(this, "Location permission needed!", Toast.LENGTH_LONG).show();
            }
        }
    }
}