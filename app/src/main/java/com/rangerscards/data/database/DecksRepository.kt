package com.rangerscards.data.database

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow


interface DecksRepository {

    suspend fun deleteAllUploadedDecks()

    suspend fun syncDecks(networkDecks: List<Deck>)

    fun getAllDecks(): Flow<PagingData<DeckListItemProjection>>

    fun searchDecks(query: String): Flow<PagingData<DeckListItemProjection>>

    fun getCard(id: String): Flow<CardListItemProjection>
}