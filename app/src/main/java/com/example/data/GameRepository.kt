package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val highScores: Flow<List<ScoreEntry>> = gameDao.getHighScores()
    val unlockedFacts: Flow<List<UnlockedHistory>> = gameDao.getUnlockedFacts()

    suspend fun insertScore(playerName: String, score: Int, level: Int) {
        val entry = ScoreEntry(
            playerName = playerName.trim().ifEmpty { "კეთილი მგზავრი" }, // Default: "Kind Traveler" in Georgian
            score = score,
            level = level
        )
        gameDao.insertScore(entry)
    }

    suspend fun unlockFact(factId: String) {
        val entry = UnlockedHistory(factId = factId)
        gameDao.unlockFact(entry)
    }

    suspend fun clearAll() {
        gameDao.clearAllScores()
        gameDao.clearUnlockedFacts()
    }
}
