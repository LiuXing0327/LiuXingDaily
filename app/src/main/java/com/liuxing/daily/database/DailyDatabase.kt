package com.liuxing.daily.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liuxing.daily.dao.DailyDao
import com.liuxing.daily.entity.DailyEntity

@Database(entities = [DailyEntity::class], version = 2, exportSchema = false)
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun getDailyDao(): DailyDao

    /**
     * 数据库升级
     *
     * MIGRATION_1_2 1 -> 2
     */
    object MIGRATION_1_2 : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN SINGLE_PASSWORD TEXT")
        }
    }
}