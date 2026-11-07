package com.example.remindergym

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAll(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder)
    @Delete
    suspend fun delete(reminder:Reminder)
}