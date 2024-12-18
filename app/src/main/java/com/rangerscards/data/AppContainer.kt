package com.rangerscards.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val apolloClient: ApolloClient
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
            .normalizedCache(SqlNormalizedCacheFactory("apollo.db"))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            //.fetchPolicy(FetchPolicy.CacheAndNetwork)
            .build()
    }
}