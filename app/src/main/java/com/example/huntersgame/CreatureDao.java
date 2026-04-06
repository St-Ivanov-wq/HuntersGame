package com.example.huntersgame;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CreatureDao {

    @Insert
    void insertAll(List<CreatureEntity> creatures);

    @Insert
    void insert(CreatureEntity creature);

    @Update
    void update(CreatureEntity creature);

    @Query("SELECT * FROM creatures ORDER BY name ASC")
    List<CreatureEntity> getAllCreatures();

    @Query("SELECT * FROM creatures WHERE id = :id")
    CreatureEntity getCreatureById(String id);

    @Query("SELECT * FROM creatures WHERE name = :name")
    CreatureEntity getCreatureByName(String name);

    @Query("UPDATE creatures SET captureCount = captureCount + 1, isCaptured = 1, " +
            "habitatUnlocked = CASE WHEN captureCount + 1 >= 2 THEN 1 ELSE habitatUnlocked END, " +
            "descriptionUnlocked = CASE WHEN captureCount + 1 >= 5 THEN 1 ELSE descriptionUnlocked END, " +
            "fullInfoUnlocked = CASE WHEN captureCount + 1 >= 10 THEN 1 ELSE fullInfoUnlocked END " +
            "WHERE name = :name")
    void incrementCaptureCount(String name);

    @Query("UPDATE creatures SET captureCount = :count, isCaptured = :captured, " +
            "habitatUnlocked = :habitatUnlocked, descriptionUnlocked = :descriptionUnlocked, " +
            "fullInfoUnlocked = :fullInfoUnlocked WHERE id = :id")
    void updateCreatureFull(String id, int count, boolean captured,
                            boolean habitatUnlocked, boolean descriptionUnlocked, boolean fullInfoUnlocked);

    @Query("SELECT COUNT(*) FROM creatures WHERE captureCount > 0")
    int getDiscoveredCount();
}