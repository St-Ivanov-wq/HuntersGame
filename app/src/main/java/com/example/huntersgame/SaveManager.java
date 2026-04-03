package com.example.huntersgame;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
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

    public void saveCreatures(List<Creature> creatures) {
        String json = gson.toJson(creatures);
        sharedPreferences.edit().putString(KEY_CREATURES, json).apply();
    }

    public List<Creature> loadCreatures() {
        String json = sharedPreferences.getString(KEY_CREATURES, "");
        if (json.isEmpty()) {
            return null;
        }
        Type type = new TypeToken<List<Creature>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void savePoints(int points) {
        sharedPreferences.edit().putInt(KEY_POINTS, points).apply();
    }

    public int loadPoints() {
        return sharedPreferences.getInt(KEY_POINTS, 0);
    }

    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
    }
}