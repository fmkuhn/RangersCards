package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.CardFilterOptions
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun insertAllCards(cards: List<Card>)

    suspend fun upsertAllCards(cards: List<Card>)

    suspend fun isExists(): Boolean

    fun getAllCards(
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardListItemProjection>>

    fun searchCards(
        filterOptions: CardFilterOptions,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String,
        taboo: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardListItemProjection>>

    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection?>
}