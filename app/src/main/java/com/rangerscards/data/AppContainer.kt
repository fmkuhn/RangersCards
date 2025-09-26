package com.rangerscards.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.network.ws.SubscriptionWsProtocol
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.rangerscards.data.database.RangersDatabase
import com.rangerscards.data.database.repository.CampaignRepository
import com.rangerscards.data.database.repository.CampaignsRepository
import com.rangerscards.data.database.repository.CardsRepository
import com.rangerscards.data.database.repository.DeckRepository
import com.rangerscards.data.database.repository.DecksRepository
import com.rangerscards.data.database.repository.OfflineCampaignRepository
import com.rangerscards.data.database.repository.OfflineCampaignsRepository
import com.rangerscards.data.database.repository.OfflineCardsRepository
import com.rangerscards.data.database.repository.OfflineDeckRepository
import com.rangerscards.data.database.repository.OfflineDecksRepository
import com.rangerscards.data.database.repository.OfflineSettingsRepository
import com.rangerscards.data.database.repository.SettingsRepository
import com.rangerscards.data.objects.JsonElementAdapter
import com.rangerscards.type.Jsonb
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private const val PREFERENCE_NAME = "settings_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val apolloClient: ApolloClient
    val userPreferencesRepository: UserPreferencesRepository
    val userAuthRepository: UserAuthRepository
    val cardsRepository: CardsRepository
    val decksRepository: DecksRepository
    val deckRepository: DeckRepository
    val campaignsRepository: CampaignsRepository
    val campaignRepository: CampaignRepository
    val settingsRepository: SettingsRepository
}

/**
 * [AppContainer] implementation that provides instance of Repositories and ApolloClient
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ApolloClient]
     */
    override val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://gapi.rangersdb.com/v1/graphql")
            .subscriptionNetworkTransport(WebSocketNetworkTransport.Builder()
                .serverUrl("wss://gapi.rangersdb.com/v1/graphql")
                .protocol(SubscriptionWsProtocol.Factory(connectionPayload = suspend {
                    val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                    mapOf("headers" to mapOf("Authorization" to "Bearer $token"))
                }))
                .reopenWhen { _, attempt ->
                    delay(attempt * 1000)
                    attempt < 5
                }
                .build()
            )
            .addCustomScalarAdapter(Jsonb.type, JsonElementAdapter)
            .normalizedCache(SqlNormalizedCacheFactory("apollo.db"))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .build()
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }

    override val userAuthRepository: UserAuthRepository by lazy {
        UserAuthRepository()
    }

    override val cardsRepository: CardsRepository by lazy {
        OfflineCardsRepository(RangersDatabase.getDatabase(context).cardDao())
    }

    override val decksRepository: DecksRepository by lazy {
        OfflineDecksRepository(RangersDatabase.getDatabase(context).deckDao())
    }

    override val deckRepository: DeckRepository by lazy {
        OfflineDeckRepository(RangersDatabase.getDatabase(context).deckDao())
    }

    override val campaignsRepository: CampaignsRepository by lazy {
        OfflineCampaignsRepository(RangersDatabase.getDatabase(context).campaignDao(),
            RangersDatabase.getDatabase(context).deckDao())
    }

    override val campaignRepository: CampaignRepository by lazy {
        OfflineCampaignRepository(RangersDatabase.getDatabase(context).campaignDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(RangersDatabase.getDatabase(context).deckDao(),
            RangersDatabase.getDatabase(context).campaignDao())
    }
}