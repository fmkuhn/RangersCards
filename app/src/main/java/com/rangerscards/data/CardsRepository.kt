package com.rangerscards.data

import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun updateAllCards(cards: List<Card>)

    suspend fun insertAllCards(cards: List<Card>)

    suspend fun isExists(): Boolean

    fun getAllCardsStream(spoiler: Boolean): Flow<List<Card>>
}