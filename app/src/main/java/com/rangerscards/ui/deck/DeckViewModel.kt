package com.rangerscards.ui.deck

import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloClient
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.data.database.repository.DeckRepository
import kotlinx.coroutines.flow.Flow

class DeckViewModel(
    private val apolloClient: ApolloClient,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    fun getDeck(id: String): Flow<Deck> = deckRepository.getDeck(id)

    fun getRole(id: String): Flow<RoleCardProjection> = deckRepository.getRole(id)
}