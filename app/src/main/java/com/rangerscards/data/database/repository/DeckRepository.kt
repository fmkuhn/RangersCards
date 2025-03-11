package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.ui.deck.DeckInfo
import kotlinx.coroutines.flow.Flow

interface DeckRepository {

    suspend fun getDeck(id: String): Deck

    suspend fun getRole(id: String): RoleCardProjection

    suspend fun updateDeck(deck: Deck)

    fun getCardsByIds(ids: List<String>): Flow<List<CardDeckListItemProjection>>

    suspend fun getChangedCardsByIds(ids: List<String>): List<CardDeckListItemProjection>

    fun getAllCards(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
    ): Flow<PagingData<CardDeckListItemProjection>>

    fun searchCards(
        searchQuery: String,
        deckInfo: DeckInfo,
        includeEnglish: Boolean,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        language: String
    ): Flow<PagingData<CardDeckListItemProjection>>
}