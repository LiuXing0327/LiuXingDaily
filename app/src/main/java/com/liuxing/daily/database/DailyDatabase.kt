package com.liuxing.daily.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.liuxing.daily.dao.DailyDao
import com.liuxing.daily.entity.DailyEntity

@Database(entities = [DailyEntity::class], version = 1, exportSchema = false)
abstract class DailyDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: DailyDatabase? = null
        fun getDatabase(context: Context): DailyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyDatabase::class.java,
                    "daily_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun getDailyDao(): DailyDao

}