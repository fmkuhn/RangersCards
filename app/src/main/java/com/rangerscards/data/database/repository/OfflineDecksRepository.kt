package com.rangerscards.data.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.DeckListItemProjection
import kotlinx.coroutines.flow.Flow


class OfflineDecksRepository(private val deckDao: DeckDao) : DecksRepository {
    override suspend fun deleteAllUploadedDecks() = deckDao.deleteAllUploadedDecks()

    override suspend fun syncDecks(networkDecks: List<Deck>) = deckDao.syncDecks(networkDecks)

    override fun getAllDecks(): Flow<PagingData<DeckListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { deckDao.getAllDecks() }
        ).flow
    }

    override fun searchDecks(query: String): Flow<PagingData<DeckListItemProjection>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alpha}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { deckDao.searchDecks(newQuery) }
        ).flow
    }

    override fun getCard(id: String): Flow<CardListItemProjection> = deckDao.getCard(id)

    override fun getRoles(specialty: String): Flow<PagingData<CardListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 5
            ),
            pagingSourceFactory = { deckDao.getRoles(specialty) }
        ).flow
    }

    override suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)
}