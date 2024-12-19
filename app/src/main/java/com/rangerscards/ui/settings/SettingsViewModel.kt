package com.rangerscards.ui.settings

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloNetworkException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.GetProfileQuery
import com.rangerscards.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel(private val apolloClient: ApolloClient) : ViewModel() {

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
                    "Invalid password. It must be 6 symbols minimum.",
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
                    "Invalid password. It must be 6 symbols minimum.",
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
        _currentUser.update { null }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }

    suspend fun getUserInfo(id: String): GetProfileQuery.Data? {
        var result: GetProfileQuery.Data? = null
        viewModelScope.launch {
            val token = currentUser.value?.getIdToken(false)?.await()?.token
            val response = apolloClient.query(GetProfileQuery(id))
                .addHttpHeader("Authorization", "Bearer $token")
                .execute()
            when {
                response.errors.orEmpty().isNotEmpty() -> {
                    // GraphQL error
                    Log.d("GraphQL error", response.errors!!.first().message)
                }
                response.exception is ApolloNetworkException -> {
                    // Network error
                    Log.d("Network error", "Please check your network connectivity.")
                }
                response.data != null -> {
                    // data (never partial)
                    result = response.data
                }
                else -> {
                    // Another fetch error, maybe a cache miss?
                    // Or potentially a non-compliant server returning data: null without an error
                    Log.d("Another fetch error", "Oh no... An error happened.")
                }
            }
        }.join()
        return result
    }
}