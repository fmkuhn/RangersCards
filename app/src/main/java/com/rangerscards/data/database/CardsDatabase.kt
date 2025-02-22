package com.rangerscards.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Card::class, CardFts::class, Deck::class], version = 1, exportSchema = true)
@TypeConverters(JsonElementConverter::class)
abstract class CardsDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao

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