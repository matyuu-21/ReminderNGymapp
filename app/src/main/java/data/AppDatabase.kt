package com.example.remindergym.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.remindergym.reminders.Reminder
import com.example.remindergym.reminders.ReminderDao
import com.example.remindergym.gym.Workout
import com.example.remindergym.gym.WorkoutDao

@Database(entities = [Reminder::class, Workout::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "remindergym.db"
                ).fallbackToDestructiveMigration(true).build().also {INSTANCE = it}
            }
        }
    }
}