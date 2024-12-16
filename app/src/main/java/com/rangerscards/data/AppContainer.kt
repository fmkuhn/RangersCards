package com.rangerscards.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    //val itemsRepository: ItemsRepository
}

/**
 * [AppContainer] implementation that provides instance of Repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
//    override val itemsRepository: ItemsRepository by lazy {
//        OfflineItemsRepository(InventoryDatabase.getDatabase(context).itemDao())
//    }
}