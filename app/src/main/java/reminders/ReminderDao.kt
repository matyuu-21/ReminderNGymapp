package com.example.remindergym.reminders

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAll(): Flow<List<Reminder>>
    @Update
    suspend fun update(reminder: Reminder)
    @Insert
    suspend fun insert(reminder: Reminder): Long
    @Delete
    suspend fun delete(reminder:Reminder)


}