package com.example.huntersgame;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;

public class VibrationManager {

    private Vibrator vibrator;
    private SettingsManager settingsManager;

    public VibrationManager(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        settingsManager = new SettingsManager(context);  // ← Uses your settings
    }

    public void vibrate(long milliseconds) {
        // ✅ Checks if user enabled vibration in settings
        if (settingsManager.isVibrationEnabled() && vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    public void vibratePattern(long[] pattern, int repeat) {
        // ✅ Also checks user preference
        if (settingsManager.isVibrationEnabled() && vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
            } else {
                vibrator.vibrate(pattern, repeat);
            }
        }
    }

    public void tap() {
        vibrate(30);
    }

    public void success() {
        vibrate(80);
    }

    public void failure() {
        vibrate(100);
    }

    public void celebration() {
        long[] pattern = {0, 80, 100, 80};
        vibratePattern(pattern, -1);
    }
}