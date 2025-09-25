package com.rangerscards.data

import com.rangerscards.data.objects.ChallengeDeck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CurrentChallengeDeck(startingIds: List<Int>) {
    private var challengeDeckIds: ArrayDeque<Int> = ArrayDeque(
        startingIds.ifEmpty { ChallengeDeck.challengeDeck.keys.shuffled() }
    )
    private val _scoutPosition = MutableStateFlow(0)
    val scoutPosition: StateFlow<Int> = _scoutPosition.asStateFlow()
    private val _size = MutableStateFlow(challengeDeckIds.size)
    val size: StateFlow<Int> = _size.asStateFlow()

    fun draw(): Int? {
        if (_scoutPosition.value > 0) resetScoutPosition()
        val drawCardId = challengeDeckIds.removeFirstOrNull()
        _size.update { challengeDeckIds.size }
        return drawCardId
    }

    fun scout(): Int? {
        val scoutCardId = challengeDeckIds.getOrNull(_scoutPosition.value)
        _scoutPosition.update { _scoutPosition.value + 1 }
        return scoutCardId
    }

    fun resetScoutPosition() {
        _scoutPosition.update { 0 }
    }

    fun reshuffle(): List<Int> {
        challengeDeckIds = ArrayDeque(ChallengeDeck.challengeDeck.keys.shuffled())
        _size.update { challengeDeckIds.size }
        return challengeDeckIds
    }

    fun getDeckAsList(): List<Int> = challengeDeckIds
}