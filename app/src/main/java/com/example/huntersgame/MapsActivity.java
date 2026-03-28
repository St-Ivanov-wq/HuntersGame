package com.example.huntersgame;

import android.Manifest;
import android.content.pm.PackageManager;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnFindNearby;
    private Button btnBack;
    private TextView tvStatus;

    private List<Creature> allCreatures;
    private List<Creature> uncapturedCreatures;
    private HashMap<Marker, Creature> creatureMarkers;
    private Location currentLocation;
    private SaveManager saveManager;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize views
        btnFindNearby = findViewById(R.id.btn_find_nearby);
        btnBack = findViewById(R.id.btn_back_maps);
        tvStatus = findViewById(R.id.tv_map_status);

        // Initialize managers
        saveManager = new SaveManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get creatures
        allCreatures = CreatureData.getCreatures();
        loadSavedData();

        creatureMarkers = new HashMap<>();

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Button listeners
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

        // Get uncaptured creatures
        uncapturedCreatures = new ArrayList<>();
        for (Creature creature : allCreatures) {
            if (!creature.isCaptured()) {
                uncapturedCreatures.add(creature);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set map click listener to show creature info
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Creature creature = creatureMarkers.get(marker);
                if (creature != null) {
                    showCreatureInfo(creature, marker);
                }
                return false;
            }
        });
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
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                    tvStatus.setText("📍 You are here! Find creatures nearby!");
                } else {
                    tvStatus.setText("⚠️ Waiting for location...");
                }
            }
        });
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

        // Clear old markers
        if (mMap != null) {
            mMap.clear();
        }
        creatureMarkers.clear();

        // Spawn 3-5 creatures around the player
        Random random = new Random();
        int creatureCount = Math.min(3 + random.nextInt(3), uncapturedCreatures.size());

        for (int i = 0; i < creatureCount; i++) {
            // Pick random creature
            int creatureIndex = random.nextInt(uncapturedCreatures.size());
            Creature creature = uncapturedCreatures.get(creatureIndex);

            // Random position within 200-500 meters
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 200 + random.nextDouble() * 300; // meters
            double latOffset = (distance / 111320) * Math.cos(angle);
            double lngOffset = (distance / (111320 * Math.cos(currentLocation.getLatitude()))) * Math.sin(angle);

            LatLng creatureLocation = new LatLng(
                    currentLocation.getLatitude() + latOffset,
                    currentLocation.getLongitude() + lngOffset
            );

            // Add marker with different color based on creature type
            float markerColor = getMarkerColor(creature.getName());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(creatureLocation)
                    .title(creature.getName() + " (" + creature.getNameBulgarian() + ")")
                    .snippet("⭐ " + creature.getPointsValue() + " points\nTap to capture!")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

            if (marker != null) {
                creatureMarkers.put(marker, creature);
            }
        }

        // Center map on player
        LatLng playerLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocation, 16));
        }

        tvStatus.setText("🔍 " + creatureCount + " mythical creatures nearby! Tap on them to capture!");
    }

    private float getMarkerColor(String creatureName) {
        switch(creatureName) {
            case "Samodiva":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "Zmey":
                return BitmapDescriptorFactory.HUE_RED;
            case "Karakondzul":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "Torbalan":
                return BitmapDescriptorFactory.HUE_VIOLET;
            case "Vila":
                return BitmapDescriptorFactory.HUE_GREEN;
            default:
                return BitmapDescriptorFactory.HUE_BLUE;
        }
    }

    private void showCreatureInfo(Creature creature, Marker marker) {
        // Show dialog with creature info and capture option
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(creature.getName() + " (" + creature.getNameBulgarian() + ")");
        builder.setMessage(
                "📖 " + creature.getDescription() + "\n\n" +
                        "🏞️ Habitat: " + creature.getHabitat() + "\n" +
                        "⭐ Worth: " + creature.getPointsValue() + " points\n\n" +
                        "Do you want to capture this creature?"
        );

        builder.setPositiveButton("Capture!", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                captureCreature(creature, marker);
            }
        });

        builder.setNegativeButton("Keep Hunting", null);
        builder.show();
    }

    private void captureCreature(Creature creature, Marker marker) {
        if (!creature.isCaptured()) {
            creature.setCaptured(true);

            // Save progress
            saveManager.saveCreatures(allCreatures);
            int currentPoints = saveManager.loadPoints();
            saveManager.savePoints(currentPoints + creature.getPointsValue());

            // Remove marker
            if (marker != null) {
                marker.remove();
            }
            creatureMarkers.remove(marker);

            // Update uncaptured list
            uncapturedCreatures.remove(creature);

            Toast.makeText(this, "✅ Captured " + creature.getName() + "! +" +
                    creature.getPointsValue() + " points!", Toast.LENGTH_LONG).show();

            tvStatus.setText("✅ Captured " + creature.getName() + "! " +
                    uncapturedCreatures.size() + " creatures left to find!");

            if (uncapturedCreatures.isEmpty()) {
                tvStatus.setText("🎉 CONGRATULATIONS! You've captured all mythical creatures! 🎉");
                btnFindNearby.setEnabled(false);
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
                        try {
                            mMap.setMyLocationEnabled(true);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission needed to find creatures!", Toast.LENGTH_LONG).show();
            }
        }
    }
}