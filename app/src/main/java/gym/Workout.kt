package com.example.remindergym.gym

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exercise: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val rir: Int,
    val date: Long = System.currentTimeMillis()
)