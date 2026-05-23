package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Scores
    @Query("SELECT * FROM scores ORDER BY score DESC, dateMillis DESC LIMIT 50")
    fun getHighScores(): Flow<List<ScoreEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntry)

    @Query("DELETE FROM scores")
    suspend fun clearAllScores()

    // Unlocked History
    @Query("SELECT * FROM unlocked_history")
    fun getUnlockedFacts(): Flow<List<UnlockedHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockFact(unlockedFact: UnlockedHistory)

    @Query("DELETE FROM unlocked_history")
    suspend fun clearUnlockedFacts()
}
