package com.rangerscards.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rangerscards.RangersApplication
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.deck.DeckViewModel
import com.rangerscards.ui.decks.DecksViewModel
import com.rangerscards.ui.settings.SettingsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Rangers app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        //Initializer for SettingsViewModel
        initializer {
            SettingsViewModel(rangersApplication().container.apolloClient,
                rangersApplication().container.userAuthRepository,
                rangersApplication().container.userPreferencesRepository,
                rangersApplication().container.cardsRepository)
        }

        //Initializer for CardsViewModel
        initializer {
            CardsViewModel(rangersApplication().container.cardsRepository,
                rangersApplication().container.userPreferencesRepository)
        }

        //Initializer for DecksViewModel
        initializer {
            DecksViewModel(rangersApplication().container.apolloClient,
                rangersApplication().container.decksRepository)
        }

        //Initializer for DeckViewModel
        initializer {
            DeckViewModel()
        }
//
//        //Initializer for CampaignsViewModel
//        initializer {
//            CampaignsViewModel()
//        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [RangersApplication].
 */
fun CreationExtras.rangersApplication(): RangersApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RangersApplication)

