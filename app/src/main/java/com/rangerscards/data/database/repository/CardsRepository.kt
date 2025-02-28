package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun updateAllCards(cards: List<Card>)

    suspend fun upsertAllCards(cards: List<Card>)

    suspend fun isExists(): Boolean

    fun getAllCards(spoiler: Boolean): Flow<PagingData<CardListItemProjection>>

    fun searchCards(
        searchQuery: String,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String
    ): Flow<PagingData<CardListItemProjection>>

    fun getCardById(cardId: String): Flow<FullCardProjection>
}