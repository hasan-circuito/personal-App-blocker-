package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MonitoredAppEntity::class,
        LockoutEntity::class,
        BlockEventEntity::class,
        AppSettingEntity::class,
        BlockedDomainEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FocusGuardDatabase : RoomDatabase() {

    abstract fun focusGuardDao(): FocusGuardDao

    companion object {
        @Volatile
        private var INSTANCE: FocusGuardDatabase? = null

        fun getDatabase(context: Context): FocusGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusGuardDatabase::class.java,
                    "focus_guard_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
