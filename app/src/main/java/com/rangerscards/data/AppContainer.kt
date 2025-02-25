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
import com.rangerscards.data.database.CardsDatabase
import com.rangerscards.data.database.repository.CardsRepository
import com.rangerscards.data.database.repository.DecksRepository
import com.rangerscards.data.database.repository.OfflineCardsRepository
import com.rangerscards.data.database.repository.OfflineDecksRepository
import com.rangerscards.data.objects.JsonElementAdapter
import com.rangerscards.type.Jsonb

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
        OfflineCardsRepository(CardsDatabase.getDatabase(context).cardDao())
    }

    override val decksRepository: DecksRepository by lazy {
        OfflineDecksRepository(CardsDatabase.getDatabase(context).deckDao())
    }
}