package com.rangerscards.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun updateAllCards(cards: List<Card>)

    suspend fun insertAllCards(cards: List<Card>)

    suspend fun isExists(): Boolean

    fun getAllCards(spoiler: Boolean): Flow<PagingData<CardListItemProjection>>

    fun searchCards(
        searchQuery: String,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String
    ): Flow<PagingData<CardListItemProjection>>
}