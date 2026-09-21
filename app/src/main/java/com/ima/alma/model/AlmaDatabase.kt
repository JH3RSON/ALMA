package com.ima.alma.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [EntradaDiario::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AlmaDatabase : RoomDatabase() {
    abstract fun diarioDao(): DiarioDao

    companion object {
        @Volatile
        private var INSTANCE: AlmaDatabase? = null

        fun getDatabase(context: Context): AlmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlmaDatabase::class.java,
                    "alma_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
