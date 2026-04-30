package com.example.huntersgame;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    private SettingsManager settingsManager;
    private SoundManager soundManager;
    private SeekBar musicSeekBar, sfxSeekBar;
    private Switch vibrationSwitch;
    private TextView musicPercent, sfxPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = new SettingsManager(this);
        soundManager = SoundManager.getInstance(this);

        musicSeekBar = findViewById(R.id.music_volume_seekbar);
        sfxSeekBar = findViewById(R.id.sfx_volume_seekbar);
        vibrationSwitch = findViewById(R.id.vibration_switch);
        musicPercent = findViewById(R.id.music_volume_percent);
        sfxPercent = findViewById(R.id.sfx_volume_percent);

        // Load current values
        float musicVol = settingsManager.getMusicVolume();
        float sfxVol = settingsManager.getSfxVolume();
        musicSeekBar.setProgress((int)(musicVol * 100));
        sfxSeekBar.setProgress((int)(sfxVol * 100));
        musicPercent.setText((int)(musicVol * 100) + "%");
        sfxPercent.setText((int)(sfxVol * 100) + "%");
        vibrationSwitch.setChecked(settingsManager.isVibrationEnabled());

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float vol = progress / 100f;
                settingsManager.setMusicVolume(vol);
                musicPercent.setText(progress + "%");
                soundManager.setMusicVolume(vol);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sfxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float vol = progress / 100f;
                settingsManager.setSfxVolume(vol);
                sfxPercent.setText(progress + "%");
                soundManager.setSfxVolume(vol);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setVibrationEnabled(isChecked);
        });

        findViewById(R.id.btn_back_settings).setOnClickListener(v -> {
            SoundManager.getInstance(this).playButtonClick();
            finish();});
    }
}