package com.rangerscards.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Card::class, CardFts::class], version = 1, exportSchema = true)
abstract class CardsDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var Instance: CardsDatabase? = null

        fun getDatabase(context: Context): CardsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CardsDatabase::class.java, "card_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}