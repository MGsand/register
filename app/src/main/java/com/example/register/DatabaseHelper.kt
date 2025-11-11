package com.example.register

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "game_database.db"
        private const val DATABASE_VERSION = 1

        // Таблица рекордов
        private const val TABLE_SCORES = "scores"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_NAME = "user_name"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_DIFFICULTY = "difficulty"
        private const val COLUMN_BUGS_DESTROYED = "bugs_destroyed"
        private const val COLUMN_ACCURACY = "accuracy"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_SCORES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_SCORE INTEGER NOT NULL,
                $COLUMN_DIFFICULTY TEXT NOT NULL,
                $COLUMN_BUGS_DESTROYED INTEGER NOT NULL,
                $COLUMN_ACCURACY REAL NOT NULL,
                $COLUMN_DATE INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    // Добавить рекорд
    fun addScore(userName: String, score: Int, difficulty: String, bugsDestroyed: Int, accuracy: Float): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, userName)
            put(COLUMN_SCORE, score)
            put(COLUMN_DIFFICULTY, difficulty)
            put(COLUMN_BUGS_DESTROYED, bugsDestroyed)
            put(COLUMN_ACCURACY, accuracy)
            put(COLUMN_DATE, System.currentTimeMillis())
        }
        val result = db.insert(TABLE_SCORES, null, values)
        return result != -1L
    }

    // Получить топ-20 рекордов
    fun getTopScores(): List<GameScore> {
        val scores = mutableListOf<GameScore>()
        val db = readableDatabase

        val query = "SELECT * FROM $TABLE_SCORES ORDER BY $COLUMN_SCORE DESC LIMIT 20"
        val cursor: Cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val score = GameScore(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                difficulty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)),
                bugsDestroyed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUGS_DESTROYED)),
                accuracy = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCURACY)),
                date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            )
            scores.add(score)
        }
        cursor.close()

        return scores
    }

    // Удалить все рекорды (опционально)
    fun clearAllScores(): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_SCORES, null, null)
        return result > 0
    }
}

// Модель данных
data class GameScore(
    val id: Long,
    val userName: String,
    val score: Int,
    val difficulty: String,
    val bugsDestroyed: Int,
    val accuracy: Float,
    val date: Long
)