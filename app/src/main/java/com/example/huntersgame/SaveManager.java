package com.example.huntersgame;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {

    private static final String PREF_NAME = "HuntersOfMythicalCreatures";
    private static final String KEY_CREATURES = "creatures_data";
    private static final String KEY_POINTS = "total_points";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SaveManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Save all creatures
    public void saveCreatures(List<Creature> creatures) {
        String json = gson.toJson(creatures);
        sharedPreferences.edit().putString(KEY_CREATURES, json).apply();
    }

    // Load creatures
    public List<Creature> loadCreatures() {
        String json = sharedPreferences.getString(KEY_CREATURES, "");
        if (json.isEmpty()) {
            return null;
        }
        Type type = new TypeToken<ArrayList<Creature>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Save points
    public void savePoints(int points) {
        sharedPreferences.edit().putInt(KEY_POINTS, points).apply();
    }

    // Load points
    public int loadPoints() {
        return sharedPreferences.getInt(KEY_POINTS, 0);
    }

    // Clear all data (for reset button)
    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
    }
}