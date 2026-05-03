package com.example.huntersgame;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class SoundManager {

    private static SoundManager instance;
    private Handler delayHandler = new Handler();
    private SettingsManager settingsManager;

    private MediaPlayer catchPlayer;
    private MediaPlayer perfectPlayer;
    private MediaPlayer missPlayer;
    private MediaPlayer throwPlayer;
    private MediaPlayer levelUpPlayer;
    private MediaPlayer buttonPlayer;

    private MediaPlayer menuOpenPlayer;
    private MediaPlayer menuClosePlayer;
    private MediaPlayer cardFlipPlayer;
    private MediaPlayer achievementPlayer;

    private MediaPlayer appLaunchPlayer;
    private MediaPlayer encounterCreaturePlayer;
    private MediaPlayer mainThemePlayer;
    private MediaPlayer huntingThemePlayer;

    private boolean huntingThemePlaying = false;
    static private boolean mainThemePlaying = false;

    private SoundManager(Context context) {
        settingsManager = new SettingsManager(context);
        loadSounds(context);
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadSounds(Context context) {
        try {
            catchPlayer = MediaPlayer.create(context, R.raw.catch_sound);
            perfectPlayer = MediaPlayer.create(context, R.raw.perfect_throw);
            missPlayer = MediaPlayer.create(context, R.raw.miss_sound);
            throwPlayer = MediaPlayer.create(context, R.raw.throw_sound);
            levelUpPlayer = MediaPlayer.create(context, R.raw.level_up);
            buttonPlayer = MediaPlayer.create(context, R.raw.button_click);

            menuOpenPlayer = MediaPlayer.create(context, R.raw.menu_open);
            menuClosePlayer = MediaPlayer.create(context, R.raw.menu_close);
            cardFlipPlayer = MediaPlayer.create(context, R.raw.card_flip);
            achievementPlayer = MediaPlayer.create(context, R.raw.achievement);

            appLaunchPlayer = MediaPlayer.create(context, R.raw.app_launch);
            encounterCreaturePlayer = MediaPlayer.create(context, R.raw.encounter_creature);
            mainThemePlayer = MediaPlayer.create(context, R.raw.main_theme);
            huntingThemePlayer = MediaPlayer.create(context, R.raw.hunting_theme);

            if (mainThemePlayer != null) mainThemePlayer.setLooping(true);
            if (huntingThemePlayer != null) huntingThemePlayer.setLooping(true);

            applyVolumes();

            Log.d("SoundManager", "All sounds loaded successfully");
        } catch (Exception e) {
            Log.e("SoundManager", "Error loading sounds: " + e.getMessage());
        }
    }

    private void applyVolumes() {
        float musicVol = settingsManager.getMusicVolume();
        float sfxVol = settingsManager.getSfxVolume();
        setMusicVolume(musicVol);
        setSfxVolume(sfxVol);
    }

    public void setMusicVolume(float volume) {
        if (mainThemePlayer != null) mainThemePlayer.setVolume(volume, volume);
        if (huntingThemePlayer != null) huntingThemePlayer.setVolume(volume, volume);
        if (appLaunchPlayer != null) appLaunchPlayer.setVolume(volume, volume);
        if (encounterCreaturePlayer != null) encounterCreaturePlayer.setVolume(volume, volume);
    }

    public void setSfxVolume(float volume) {
        if (catchPlayer != null) catchPlayer.setVolume(volume, volume);
        if (perfectPlayer != null) perfectPlayer.setVolume(volume, volume);
        if (missPlayer != null) missPlayer.setVolume(volume, volume);
        if (throwPlayer != null) throwPlayer.setVolume(volume, volume);
        if (levelUpPlayer != null) levelUpPlayer.setVolume(volume, volume);
        if (buttonPlayer != null) buttonPlayer.setVolume(volume, volume);
        if (menuOpenPlayer != null) menuOpenPlayer.setVolume(volume, volume);
        if (menuClosePlayer != null) menuClosePlayer.setVolume(volume, volume);
        if (cardFlipPlayer != null) cardFlipPlayer.setVolume(volume, volume);
        if (achievementPlayer != null) achievementPlayer.setVolume(volume, volume);
    }

    public void playCatchSound() {
        if (catchPlayer != null) {
            if (catchPlayer.isPlaying()) catchPlayer.seekTo(0);
            catchPlayer.start();
        }
    }

    public boolean isHuntingThemePlaying() {
        return huntingThemePlaying;
    }

    static public boolean isMainThemePlaying() {
        return mainThemePlaying;
    }
    public void playPerfectThrowSound() {
        if (perfectPlayer != null) {
            if (perfectPlayer.isPlaying()) perfectPlayer.seekTo(0);
            perfectPlayer.start();
        }
    }

    public void playMissSound() {
        if (missPlayer != null) {
            if (missPlayer.isPlaying()) missPlayer.seekTo(0);
            missPlayer.start();
        }
    }

    public void playThrowSound() {
        if (throwPlayer != null) {
            if (throwPlayer.isPlaying()) throwPlayer.seekTo(0);
            throwPlayer.start();
        }
    }

    public void playLevelUpSound() {
        if (levelUpPlayer != null) {
            if (levelUpPlayer.isPlaying()) levelUpPlayer.seekTo(0);
            levelUpPlayer.start();
        }
    }

    public void playButtonClick() {
        if (buttonPlayer != null) {
            if (buttonPlayer.isPlaying()) buttonPlayer.seekTo(0);
            buttonPlayer.start();
        }
    }

    public void playMenuOpen() {
        if (menuOpenPlayer != null) {
            if (menuOpenPlayer.isPlaying()) menuOpenPlayer.seekTo(0);
            menuOpenPlayer.start();
        }
    }

    public void playMenuClose() {
        if (menuClosePlayer != null) {
            if (menuClosePlayer.isPlaying()) menuClosePlayer.seekTo(0);
            menuClosePlayer.start();
        }
    }

    public void playCardFlip() {
        if (cardFlipPlayer != null) {
            if (cardFlipPlayer.isPlaying()) cardFlipPlayer.seekTo(0);
            cardFlipPlayer.start();
        }
    }

    public void playAchievement() {
        if (achievementPlayer != null) {
            if (achievementPlayer.isPlaying()) achievementPlayer.seekTo(0);
            achievementPlayer.start();
        }
    }

    public void playAppLaunch() {
        if (appLaunchPlayer != null) {
            if (appLaunchPlayer.isPlaying()) appLaunchPlayer.seekTo(0);
            appLaunchPlayer.start();
        }
    }

    public void playEncounterCreature() {
        if (encounterCreaturePlayer != null) {
            if (encounterCreaturePlayer.isPlaying()) encounterCreaturePlayer.seekTo(0);
            encounterCreaturePlayer.start();
        }
    }

    public void playMainTheme() {
        if (mainThemePlayer != null && !mainThemePlayer.isPlaying()) {
            mainThemePlayer.start();
            mainThemePlaying = true;
        }
    }

    public void stopMainTheme() {
        if (mainThemePlayer != null && mainThemePlayer.isPlaying()) {
            mainThemePlayer.pause();
            mainThemePlayer.seekTo(0);
            mainThemePlaying = false;
        }
    }

    public void playHuntingTheme() {
        if (huntingThemePlayer != null && !huntingThemePlayer.isPlaying()) {
            huntingThemePlayer.start();
            huntingThemePlaying = true;
        }
    }

    public void stopHuntingTheme() {
        if (huntingThemePlayer != null && huntingThemePlayer.isPlaying()) {
            huntingThemePlayer.pause();
            huntingThemePlayer.seekTo(0);
            huntingThemePlaying = false;
        }
    }
}