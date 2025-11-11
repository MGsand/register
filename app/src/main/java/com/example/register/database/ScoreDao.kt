package com.example.register.database

import androidx.room.*

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: Score)

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT :limit")
    suspend fun getTopScores(limit: Int = 10): List<Score>

    @Query("""
        SELECT scores.*, users.fullName as userName 
        FROM scores 
        INNER JOIN users ON scores.userId = users.id 
        ORDER BY scores.score DESC 
        LIMIT :limit
    """)
    suspend fun getTopScoresWithUsers(limit: Int = 10): List<ScoreWithUser>
}

data class ScoreWithUser(
    val id: Long,
    val userId: Long,
    val score: Int,
    val difficulty: String,
    val bugsDestroyed: Int,
    val accuracy: Float,
    val gameDate: Long,
    val userName: String
)