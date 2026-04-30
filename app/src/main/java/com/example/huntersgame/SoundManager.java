package com.example.huntersgame;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class SoundManager {

    private static SoundManager instance;
    private Handler delayHandler = new Handler();
    private SettingsManager settingsManager;

    // Gameplay sounds
    private MediaPlayer catchPlayer;
    private MediaPlayer perfectPlayer;
    private MediaPlayer missPlayer;
    private MediaPlayer throwPlayer;
    private MediaPlayer levelUpPlayer;
    private MediaPlayer buttonPlayer;

    // UI sounds
    private MediaPlayer menuOpenPlayer;
    private MediaPlayer menuClosePlayer;
    private MediaPlayer cardFlipPlayer;
    private MediaPlayer achievementPlayer;

    // Music
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
            // Gameplay sounds
            catchPlayer = MediaPlayer.create(context, R.raw.catch_sound);
            perfectPlayer = MediaPlayer.create(context, R.raw.perfect_throw);
            missPlayer = MediaPlayer.create(context, R.raw.miss_sound);
            throwPlayer = MediaPlayer.create(context, R.raw.throw_sound);
            levelUpPlayer = MediaPlayer.create(context, R.raw.level_up);
            buttonPlayer = MediaPlayer.create(context, R.raw.button_click);

            // UI sounds
            menuOpenPlayer = MediaPlayer.create(context, R.raw.menu_open);
            menuClosePlayer = MediaPlayer.create(context, R.raw.menu_close);
            cardFlipPlayer = MediaPlayer.create(context, R.raw.card_flip);
            achievementPlayer = MediaPlayer.create(context, R.raw.achievement);

            // Music
            appLaunchPlayer = MediaPlayer.create(context, R.raw.app_launch);
            encounterCreaturePlayer = MediaPlayer.create(context, R.raw.encounter_creature);
            mainThemePlayer = MediaPlayer.create(context, R.raw.main_theme);
            huntingThemePlayer = MediaPlayer.create(context, R.raw.hunting_theme);

            // Set looping for themes
            if (mainThemePlayer != null) mainThemePlayer.setLooping(true);
            if (huntingThemePlayer != null) huntingThemePlayer.setLooping(true);

            // Apply saved volumes
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

    // Gameplay sounds
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

    // UI sounds
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

    // Music
    public void playAppLaunch() {
        if (appLaunchPlayer != null) {
            if (appLaunchPlayer.isPlaying()) appLaunchPlayer.seekTo(0);
            appLaunchPlayer.start();
        }
    }

    public void playAppLaunchWithDelay() {
        if (appLaunchPlayer != null) {
            if (appLaunchPlayer.isPlaying()) appLaunchPlayer.seekTo(0);
            appLaunchPlayer.start();

            int duration = appLaunchPlayer.getDuration();
            delayHandler.postDelayed(() -> playMainTheme(), duration + 200);
        } else {
            playMainTheme();
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

    public void release() {
        delayHandler.removeCallbacksAndMessages(null);

        // Gameplay sounds
        if (catchPlayer != null) { catchPlayer.release(); catchPlayer = null; }
        if (perfectPlayer != null) { perfectPlayer.release(); perfectPlayer = null; }
        if (missPlayer != null) { missPlayer.release(); missPlayer = null; }
        if (throwPlayer != null) { throwPlayer.release(); throwPlayer = null; }
        if (levelUpPlayer != null) { levelUpPlayer.release(); levelUpPlayer = null; }
        if (buttonPlayer != null) { buttonPlayer.release(); buttonPlayer = null; }

        // UI sounds
        if (menuOpenPlayer != null) { menuOpenPlayer.release(); menuOpenPlayer = null; }
        if (menuClosePlayer != null) { menuClosePlayer.release(); menuClosePlayer = null; }
        if (cardFlipPlayer != null) { cardFlipPlayer.release(); cardFlipPlayer = null; }
        if (achievementPlayer != null) { achievementPlayer.release(); achievementPlayer = null; }

        // Music
        if (appLaunchPlayer != null) { appLaunchPlayer.release(); appLaunchPlayer = null; }
        if (encounterCreaturePlayer != null) { encounterCreaturePlayer.release(); encounterCreaturePlayer = null; }
        if (mainThemePlayer != null) { mainThemePlayer.release(); mainThemePlayer = null; }
        if (huntingThemePlayer != null) { huntingThemePlayer.release(); huntingThemePlayer = null; }
    }

}