package com.rangerscards.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rangerscards.RangersApplication
import com.rangerscards.ui.settings.SettingsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Rangers app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        //Initializer for SettingsViewModel
        initializer {
            SettingsViewModel(rangersApplication().container.apolloClient,
                rangersApplication().container.userPreferencesRepository)
        }

        //Initializer for CardsViewModel
//        initializer {
//            CardsViewModel()
//        }
//
//        //Initializer for DecksViewModel
//        initializer {
//            DecksViewModel()
//        }
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

