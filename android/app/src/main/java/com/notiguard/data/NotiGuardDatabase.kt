package com.notiguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NotificationRecord::class, AppRule::class],
    version = 2,
    exportSchema = true,
)
abstract class NotiGuardDatabase : RoomDatabase() {

    abstract fun dao(): NotiGuardDao

    companion object {
        // Remove only legacy demo rows, before any queries can display them.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM records WHERE source = 'seed'")
            }
        }
        @Volatile
        private var instance: NotiGuardDatabase? = null

        fun get(context: Context): NotiGuardDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotiGuardDatabase::class.java,
                    "notiguard.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

