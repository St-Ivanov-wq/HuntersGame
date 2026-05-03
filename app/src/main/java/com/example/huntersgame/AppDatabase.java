package com.example.huntersgame;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {CreatureEntity.class, ChallengeEntity.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract CreatureDao creatureDao();
    public abstract ChallengeDao challengeDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE creatures ADD COLUMN habitatUnlocked INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE creatures ADD COLUMN descriptionUnlocked INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE creatures ADD COLUMN fullInfoUnlocked INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS challenges (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "target INTEGER, " +
                    "progress INTEGER, " +
                    "rewardXP INTEGER, " +
                    "isCompleted INTEGER, " +
                    "isClaimed INTEGER, " +
                    "lastResetDate TEXT)");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "creature_database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}