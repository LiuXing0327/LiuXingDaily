package com.liuxing.daily.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liuxing.daily.dao.DailyDao
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity

@Database(
    entities = [DailyEntity::class, DailyImageEntity::class],
    version = 4,
    exportSchema = false
)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
     *
     * 新增字段SINGLE_PASSWORD
     */
    object MIGRATION_1_2 : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN SINGLE_PASSWORD TEXT")
        }
    }

    /**
     * 数据库升级
     *
     * MIGRATION_2_3 2 -> 3
     *
     * 新增字段MOOD
     * 新增字段WEATHER
     * 新增字段DAILY_UUID
     */
    object MIGRATION_2_3 : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN MOOD INTEGER")
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN WEATHER INTEGER")
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN DAILY_UUID TEXT")
        }
    }

    /**
     * 数据库升级
     *
     * MIGRATION_3_4 3 -> 4
     *
     * 新增DAILY_IMAGE表
     */
    object MIGRATION_3_4 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `DAILY_IMAGE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `DAILY_UUID` TEXT, `IMAGE_PATH` TEXT)")
        }

    }
}