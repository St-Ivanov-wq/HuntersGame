package com.example.huntersgame;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {

    private static DatabaseManager instance;
    private AppDatabase db;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ChallengeCallback challengeCallback;

    private DatabaseManager(Context context) {
        db = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initializeDatabase();
        ensureAllCreaturesExist();
        initializeChallenges();
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeDatabase() {
        executorService.execute(() -> {
            List<CreatureEntity> existing = db.creatureDao().getAllCreatures();
            android.util.Log.d("DatabaseManager", "=== EXISTING CREATURES COUNT: " + existing.size());

            if (existing.isEmpty()) {
                List<CreatureEntity> creatures = getDefaultCreatures();
                android.util.Log.d("DatabaseManager", "=== INSERTING " + creatures.size() + " CREATURES ===");
                for (CreatureEntity c : creatures) {
                    android.util.Log.d("DatabaseManager", "Inserting: " + c.getName());
                }
                db.creatureDao().insertAll(creatures);
                android.util.Log.d("DatabaseManager", "Database initialized successfully!");
            } else {
                android.util.Log.d("DatabaseManager", "Database already has " + existing.size() + " creatures");
                for (CreatureEntity c : existing) {
                    android.util.Log.d("DatabaseManager", "Existing: " + c.getName());
                }
            }
        });
    }

    private void initializeChallenges() {
        executorService.execute(() -> {
            if (db.challengeDao().getAllChallenges().isEmpty()) {
                generateDailyChallenges();
            }
            checkDailyReset();
        });
    }

    private void checkDailyReset() {
        String today = getTodayDate();
        List<ChallengeEntity> challenges = db.challengeDao().getAllChallenges();
        for (ChallengeEntity challenge : challenges) {
            if (!challenge.getLastResetDate().equals(today)) {
                db.challengeDao().resetChallenge(challenge.getId());
                challenge.setLastResetDate(today);
                db.challengeDao().update(challenge);
            }
        }
    }

    private String getTodayDate() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return cal.get(java.util.Calendar.YEAR) + "-" + (cal.get(java.util.Calendar.MONTH) + 1) + "-" + cal.get(java.util.Calendar.DAY_OF_MONTH);
    }

    public void generateDailyChallenges() {
        executorService.execute(() -> {
            List<ChallengeEntity> existing = db.challengeDao().getAllChallenges();
            for (ChallengeEntity c : existing) {
                db.challengeDao().delete(c);
            }

            List<ChallengeEntity> newChallenges = getRandomChallenges();
            db.challengeDao().insertAll(newChallenges);

            mainHandler.post(() -> {
                if (challengeCallback != null) {
                    challengeCallback.onChallengesReset();
                }
            });
        });
    }

    private void ensureAllCreaturesExist() {
        executorService.execute(() -> {
            List<CreatureEntity> existing = db.creatureDao().getAllCreatures();
            List<CreatureEntity> defaultCreatures = getDefaultCreatures();

            android.util.Log.d("DatabaseManager", "=== ENSURING ALL CREATURES EXIST ===");
            android.util.Log.d("DatabaseManager", "Existing: " + existing.size());
            android.util.Log.d("DatabaseManager", "Default: " + defaultCreatures.size());

            for (CreatureEntity defaultCreature : defaultCreatures) {
                boolean exists = false;
                for (CreatureEntity existingCreature : existing) {
                    if (existingCreature.getName().equals(defaultCreature.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    android.util.Log.d("DatabaseManager", "Adding missing creature: " + defaultCreature.getName());
                    db.creatureDao().insert(defaultCreature);
                } else {
                    android.util.Log.d("DatabaseManager", "Already exists: " + defaultCreature.getName());
                }
            }

            List<CreatureEntity> finalList = db.creatureDao().getAllCreatures();
            android.util.Log.d("DatabaseManager", "=== FINAL CREATURE COUNT: " + finalList.size() + " ===");
            for (CreatureEntity c : finalList) {
                android.util.Log.d("DatabaseManager", " - " + c.getImageResId());
            }
        });
    }

    public void saveCreatures(List<CreatureEntity> creatures) {
        executorService.execute(() -> {
            for (CreatureEntity creature : creatures) {
                db.creatureDao().update(creature);
            }
        });
    }

    private List<ChallengeEntity> getRandomChallenges() {
        List<ChallengeEntity> challenges = new java.util.ArrayList<>();
        Random random = new Random();

        String[][] challengePool = {
                {"catch_creatures", "Catch 5 creatures", "catch", "5", "40"},
                {"perfect_throws", "Make 3 perfect throws", "perfect", "3", "45"},
                {"capture_any", "Capture any 8 creatures", "total", "8", "35"},
                {"streak_3", "Get 3 hits in a row", "streak", "3", "50"},
                {"catch_samodiva", "Catch 3 Samodivas", "catch_specific", "3", "60"},
                {"catch_zmey", "Catch 2 Zmeys", "catch_specific", "2", "70"},
                {"perfect_streak", "Get 5 perfect throws", "perfect", "5", "75"},
                {"catch_different", "Catch 4 different species", "species", "4", "65"},
                {"catch_karakondzul", "Catch 3 Karakondzuls", "catch_specific", "3", "90"},
                {"catch_torbalan", "Catch 3 Torbalans", "catch_specific", "3", "95"},
                {"catch_vila", "Catch 4 Vilas", "catch_specific", "4", "100"},
                {"master_hunter", "Catch 15 creatures total", "total", "15", "120"},
                {"perfect_master", "Get 10 perfect throws", "perfect", "10", "110"},
                {"complete_collection", "Catch all 14 species", "species", "14", "250"}
        };

        List<Integer> selectedIndices = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int index;
            do {
                index = random.nextInt(challengePool.length);
            } while (selectedIndices.contains(index));
            selectedIndices.add(index);

            String[] c = challengePool[index];
            String id = c[0] + "_" + System.currentTimeMillis() + "_" + i;
            String name = c[1];
            String type = c[2];
            int target = Integer.parseInt(c[3]);
            int rewardXP = Integer.parseInt(c[4]);

            challenges.add(new ChallengeEntity(id, name, c[1], type, target, rewardXP));
        }

        return challenges;
    }

    public void manualResetChallenges(Callback<Boolean> callback) {
        executorService.execute(() -> {
            generateDailyChallenges();
            mainHandler.post(() -> callback.onSuccess(true));
        });
    }

    public void resetAllProgress(Callback<Boolean> callback) {
        executorService.execute(() -> {
            List<CreatureEntity> creatures = db.creatureDao().getAllCreatures();
            for (CreatureEntity creature : creatures) {
                db.creatureDao().updateCreatureFull(creature.getId(), 0, false, false, false, false);
            }
            generateDailyChallenges();
            mainHandler.post(() -> callback.onSuccess(true));
        });
    }

    public void getAllCreatures(Callback<List<CreatureEntity>> callback) {
        executorService.execute(() -> {
            List<CreatureEntity> creatures = db.creatureDao().getAllCreatures();
            mainHandler.post(() -> callback.onSuccess(creatures));
        });
    }

    public void getCreatureByName(String name, Callback<CreatureEntity> callback) {
        executorService.execute(() -> {
            CreatureEntity creature = db.creatureDao().getCreatureByName(name);
            mainHandler.post(() -> callback.onSuccess(creature));
        });
    }

    public void incrementCaptureCount(String name, Callback<Integer> callback) {
        executorService.execute(() -> {
            CreatureEntity creature = db.creatureDao().getCreatureByName(name);
            if (creature != null) {
                int newCount = creature.getCaptureCount() + 1;
                boolean habitatUnlocked = creature.isHabitatUnlocked() || (newCount >= 2);
                boolean descUnlocked = creature.isDescriptionUnlocked() || (newCount >= 5);
                boolean fullUnlocked = creature.isFullInfoUnlocked() || (newCount >= 10);

                db.creatureDao().updateCreatureFull(creature.getId(), newCount, true,
                        habitatUnlocked, descUnlocked, fullUnlocked);
                mainHandler.post(() -> callback.onSuccess(newCount));
            } else {
                mainHandler.post(() -> callback.onError("Creature not found: " + name));
            }
        });
    }

    public void getAllChallenges(Callback<List<ChallengeEntity>> callback) {
        executorService.execute(() -> {
            List<ChallengeEntity> challenges = db.challengeDao().getAllChallenges();
            mainHandler.post(() -> callback.onSuccess(challenges));
        });
    }

    public void recordCapture(String creatureName, boolean wasPerfect, Callback<Void> callback) {
        executorService.execute(() -> {
            List<ChallengeEntity> challenges = db.challengeDao().getAllChallenges();

            for (ChallengeEntity challenge : challenges) {
                if (challenge.isCompleted() || challenge.isClaimed()) continue;

                String type = challenge.getType();

                switch (type) {
                    case "catch":
                        challenge.incrementProgress();
                        db.challengeDao().update(challenge);
                        break;
                    case "perfect":
                        if (wasPerfect) {
                            challenge.incrementProgress();
                            db.challengeDao().update(challenge);
                        }
                        break;
                    case "total":
                        challenge.incrementProgress();
                        db.challengeDao().update(challenge);
                        break;
                    case "catch_specific":
                        if (challenge.getName().toLowerCase().contains(creatureName.toLowerCase())) {
                            challenge.incrementProgress();
                            db.challengeDao().update(challenge);
                        }
                        break;
                    case "species":
                        int uniqueSpecies = db.creatureDao().getDiscoveredCount();
                        challenge.setProgress(uniqueSpecies);
                        db.challengeDao().update(challenge);
                        break;
                    case "streak":
                        challenge.incrementProgress();
                        db.challengeDao().update(challenge);
                        break;
                }
            }

            mainHandler.post(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    public void claimAllRewards(Callback<Integer> callback) {
        executorService.execute(() -> {
            int totalXP = db.challengeDao().getAvailableRewards();
            List<ChallengeEntity> challenges = db.challengeDao().getAllChallenges();
            for (ChallengeEntity challenge : challenges) {
                if (challenge.isCompleted() && !challenge.isClaimed()) {
                    db.challengeDao().markAsClaimed(challenge.getId());
                }
            }
            mainHandler.post(() -> callback.onSuccess(totalXP));
        });
    }

    public void getTotalXP(Callback<Integer> callback) {
        executorService.execute(() -> {
            List<CreatureEntity> creatures = db.creatureDao().getAllCreatures();
            int totalXP = 0;
            for (CreatureEntity creature : creatures) {
                totalXP += creature.getPointsValue() * creature.getCaptureCount();
            }
            final int finalXP = totalXP;
            mainHandler.post(() -> callback.onSuccess(finalXP));
        });
    }

    public void saveXP(int newTotalXP, Callback<Boolean> callback) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(true));
        }
    }

    public void setChallengeCallback(ChallengeCallback callback) {
        this.challengeCallback = callback;
    }

    private List<CreatureEntity> getDefaultCreatures() {
        List<CreatureEntity> creatures = new java.util.ArrayList<>();

        creatures.add(new CreatureEntity("1", "Samodiva", "Самодива",
                "Beautiful woodland fairies...", "Forests and mountains", 100, R.drawable.samodiva));
        creatures.add(new CreatureEntity("2", "Zmey", "Змей",
                "A dragon-like creature...", "Mountains and caves", 150, R.drawable.zmey));
        creatures.add(new CreatureEntity("3", "Karakondzul", "Караконджул",
                "A mischievous goblin...", "Villages and forests", 80, R.drawable.karakondzul));
        creatures.add(new CreatureEntity("4", "Torbalan", "Торбалан",
                "A vampire-like creature...", "Graveyards", 120, R.drawable.torbalan));
        creatures.add(new CreatureEntity("5", "Vila", "Вила",
                "Mountain spirits...", "Mountain peaks", 110, R.drawable.vila));
        creatures.add(new CreatureEntity("6", "Poludnitsa", "Полудница",
                "A noon demon...", "Fields and meadows", 90, R.drawable.poludnitsa));
        creatures.add(new CreatureEntity("7", "Talasam", "Таласъм",
                "A water spirit...", "Rivers and lakes", 130, R.drawable.talasam));
        creatures.add(new CreatureEntity("8", "Lamia", "Ламия",
                "A multi-headed serpent...", "Caves and mountains", 180, R.drawable.lamia));
        creatures.add(new CreatureEntity("10", "Orisnizi", "Орисници",
                "A trio of female creatures...", "Abandoned places", 95, R.drawable.orisnizi));
        creatures.add(new CreatureEntity("11", "Hala", "Хала",
                "A female demonic creature...", "Mountains", 160, R.drawable.hala));
        creatures.add(new CreatureEntity("12", "Borislav", "Борислав",
                "A noble forest guardian...", "Forest clearings", 125, R.drawable.borislav));
        creatures.add(new CreatureEntity("13", "Chuma", "Чума",
                "An ugly old woman...", "Dark forests", 85, R.drawable.chuma));
        creatures.add(new CreatureEntity("14", "Drekavac", "Дрекавац",
                "A screaming creature...", "Swamps and forests", 105, R.drawable.drekavac));
        creatures.add(new CreatureEntity("15", "Rusalka", "Русалка",
                "A water nymph...", "Lakes and rivers", 115, R.drawable.rusalka));

        return creatures;
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface ChallengeCallback {
        void onChallengesReset();
    }
}