package com.example.remindergym.gym

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("Select * FROM workouts")
    fun getAll(): Flow<List<Workout>>

    @Insert
    suspend fun insert(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)
}
