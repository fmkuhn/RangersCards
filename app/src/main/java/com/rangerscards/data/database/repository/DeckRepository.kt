package com.rangerscards.data.database.repository

import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

interface DeckRepository {

    suspend fun getDeck(id: String): Deck

    suspend fun getRole(id: String): RoleCardProjection

    suspend fun updateDeck(deck: Deck)

    fun getCardsByIds(ids: List<String>): Flow<List<CardDeckListItemProjection>>

    suspend fun getChangedCardsByIds(ids: List<String>): List<CardDeckListItemProjection>
}