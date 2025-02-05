package com.rangerscards.ui.cards

import androidx.lifecycle.ViewModel
import com.rangerscards.data.Card
import com.rangerscards.data.CardsRepository
import kotlinx.coroutines.flow.Flow

class CardsViewModel(
    private val cardsRepository: CardsRepository
) : ViewModel() {
    fun getAllCards(spoiler: Boolean): Flow<List<Card>> = cardsRepository.getAllCardsStream(spoiler)
}