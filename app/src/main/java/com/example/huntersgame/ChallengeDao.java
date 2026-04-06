package com.example.huntersgame;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ChallengeDao {

    @Insert
    void insert(ChallengeEntity challenge);

    @Insert
    void insertAll(List<ChallengeEntity> challenges);

    @Update
    void update(ChallengeEntity challenge);

    @Delete
    void delete(ChallengeEntity challenge);

    @Query("SELECT * FROM challenges ORDER BY rewardXP ASC")
    List<ChallengeEntity> getAllChallenges();

    @Query("SELECT * FROM challenges WHERE id = :id")
    ChallengeEntity getChallengeById(String id);

    @Query("UPDATE challenges SET progress = :progress, isCompleted = :isCompleted WHERE id = :id")
    void updateProgress(String id, int progress, boolean isCompleted);

    @Query("UPDATE challenges SET isClaimed = 1 WHERE id = :id")
    void markAsClaimed(String id);

    @Query("UPDATE challenges SET progress = 0, isCompleted = 0, isClaimed = 0 WHERE id = :id")
    void resetChallenge(String id);

    @Query("SELECT SUM(rewardXP) FROM challenges WHERE isClaimed = 0 AND isCompleted = 1")
    int getAvailableRewards();
}