package com.rangerscards.ui.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel() : ViewModel() {

    private var currentUser: FirebaseUser? = null
    init {
        if (Firebase.auth.currentUser != null) currentUser = Firebase.auth.currentUser
    }
    fun getUser() = currentUser

    fun setUser(user: FirebaseUser?) {
        currentUser = user
    }
}