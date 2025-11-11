package com.example.register.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "scores",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Score(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val score: Int,
    val difficulty: String,
    val bugsDestroyed: Int,
    val accuracy: Float,
    val gameDate: Long = System.currentTimeMillis()
)