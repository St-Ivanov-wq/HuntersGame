package com.example.huntersgame;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "HunterSettings";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_VIBRATION = "vibration";

    private SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Hunter");
    }

    public void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, 0.3f);
    }

    public void setMusicVolume(float volume) {
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, volume).apply();
    }

    public float getSfxVolume() {
        return prefs.getFloat(KEY_SFX_VOLUME, 0.7f);
    }

    public void setSfxVolume(float volume) {
        prefs.edit().putFloat(KEY_SFX_VOLUME, volume).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

}