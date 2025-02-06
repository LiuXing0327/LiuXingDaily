package com.liuxing.daily.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liuxing.daily.dao.DailyDao
import com.liuxing.daily.dao.DailyLabelDao
import com.liuxing.daily.entity.DailyAudioEntity
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyVideoEntity

@Database(
    entities = [DailyEntity::class, DailyImageEntity::class, DailyLabelEntity::class, DailyVideoEntity::class,DailyAudioEntity::class],
    version = 9,
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun getDailyDao(): DailyDao
    abstract fun getDailyLabelDao(): DailyLabelDao

    /**
     * 数据库升级
     *
     * MIGRATION_1_2 1 -> 2
     *
     * 新增字段 SINGLE_PASSWORD
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
     * 新增字段 MOOD
     * 新增字段 WEATHER
     * 新增字段 DAILY_UUID
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
     * 新增 DAILY_IMAGE 表
     */
    object MIGRATION_3_4 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `DAILY_IMAGE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `DAILY_UUID` TEXT, `IMAGE_PATH` TEXT)")
        }

    }

    /**
     * 数据库升级
     *
     * MIGRATION_4_5 4 -> 5
     *
     * 新增字段 IS_DELETED
     */
    object MIGRATION_4_5 : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN IS_DELETED INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * 数据库升级
     *
     * MIGRATION_5_6 5 -> 6
     *
     * 新增 DAILY_LABEL 表
     */
    object MIGRATION_5_6 : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `DAILY_LABEL` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `LABEL` TEXT)")
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN DAILY_LABEL TEXT")
        }
    }


    /**
     * 数据库升级
     *
     * MIGRATION_6_7 6 -> 7
     *
     * 新增 DAILY_VIDEO 表
     */
    object MIGRATION_6_7 : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `DAILY_VIDEO` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `DAILY_UUID` TEXT,`VIDEO_PATH` TEXT)")
        }
    }

    /**
     * 数据库升级
     *
     * MIGRATION_7_8 7 -> 8
     *
     * 新增 DAILY_AUDIO 表
     */
    object MIGRATION_7_8 : Migration(7,8){
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `DAILY_AUDIO` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `DAILY_UUID` TEXT,`AUDIO_PATH` TEXT)")
        }

    }

    /**
     * 数据库升级
     *
     * MIGRATION_8_9 8 -> 9
     *
     * 新增字段 DAILY_RECYCLER_DATE_TIME
     */
    object MIGRATION_8_9 : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE DAILY_INFO ADD COLUMN DAILY_RECYCLER_DATE_TIME BIGINT")
        }
    }
}