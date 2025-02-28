package com.rangerscards.data.database.repository

import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

class OfflineDeckRepository(val deckDao: DeckDao) : DeckRepository {

    override fun getDeck(id: String): Flow<Deck> = deckDao.getDeckById(id)

    override fun getRole(id: String): Flow<RoleCardProjection> = deckDao.getRole(id)
}