package com.rangerscards.data.database.repository

import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

class OfflineDeckRepository(val deckDao: DeckDao) : DeckRepository {

    override suspend fun getDeck(id: String): Deck = deckDao.getDeckById(id)

    override suspend fun getRole(id: String): RoleCardProjection = deckDao.getRole(id)

    override suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    override fun getCardsByIds(ids: List<String>): Flow<List<CardDeckListItemProjection>> =
        deckDao.getCardsByIds(ids)

    override suspend fun getChangedCardsByIds(ids: List<String>): List<CardDeckListItemProjection> =
        deckDao.getChangedCardsByIds(ids)
}