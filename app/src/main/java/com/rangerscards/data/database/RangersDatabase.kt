package com.rangerscards.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.campaign.ChallengeDeck
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardFts
import com.rangerscards.data.database.dao.CampaignDao
import com.rangerscards.data.database.dao.CardDao
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.migrations.MigrationCampaignChallengeDeck
import com.rangerscards.data.database.migrations.MigrationCampaignExpansions
import com.rangerscards.data.database.migrations.MigrationCampaignTransfer
import com.rangerscards.data.database.migrations.MigrationRemoveFlavorFromFTS
import com.rangerscards.data.objects.JsonElementConverter

@Database(entities = [Card::class, CardFts::class, Deck::class, Campaign::class, ChallengeDeck::class],
    version = 5,
    exportSchema = false)
@TypeConverters(JsonElementConverter::class)
abstract class RangersDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao
    abstract fun campaignDao(): CampaignDao

    companion object {
        @Volatile
        private var Instance: RangersDatabase? = null

        fun getDatabase(context: Context): RangersDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, RangersDatabase::class.java, "rangers_database")
                    .addMigrations(MigrationCampaignTransfer,
                        MigrationCampaignChallengeDeck,
                        MigrationRemoveFlavorFromFTS,
                        MigrationCampaignExpansions,
                    )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}