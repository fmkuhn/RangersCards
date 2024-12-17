package com.rangerscards.ui.settings

import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow(Firebase.auth.currentUser)
    var currentUser = _currentUser.asStateFlow()

    fun setUser(user: FirebaseUser?) {
        _currentUser.update { user }
    }

    fun signIn(mainActivity: MainActivity, email: String, password: String) {
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.signIn(email, password)
            } else {
                Toast.makeText(
                    mainActivity.baseContext,
                    "Invalid password.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } else {
            Toast.makeText(
                mainActivity.baseContext,
                "Invalid email.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun createAccount(mainActivity: MainActivity, email: String, password: String) {
        if (validateEmail(email)) {
            if (validatePassword(password)) {
            mainActivity.createAccount(email, password)
            } else {
                Toast.makeText(
                    mainActivity.baseContext,
                    "Invalid password.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } else {
            Toast.makeText(
                mainActivity.baseContext,
                "Invalid email.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun signOut(mainActivity: MainActivity) {
        mainActivity.signOut()
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }
}