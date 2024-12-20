package com.rangerscards.ui.settings

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloNetworkException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.GetProfileByHandleQuery
import com.rangerscards.GetProfileQuery
import com.rangerscards.MainActivity
import com.rangerscards.UpdateHandleMutation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale


/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel(private val apolloClient: ApolloClient) : ViewModel() {

    private val _userUiState = MutableStateFlow(UserUIState())
    var userUiState = _userUiState.asStateFlow()

    fun setUser(user: FirebaseUser?) {
        _userUiState.update {
            it.copy(currentUser = user)
        }
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
                    "Invalid password. It must be 6 characters minimum.",
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
        _userUiState.update {
            it.copy(currentUser = null, userInfo = null)
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }

    private fun normalizeHandle(handle: String): String {
        return handle.replace("[\\.\\$\\[\\]#/]".toRegex(),"_").lowercase(Locale.ENGLISH).trim()
    }

    suspend fun getUserInfo(id: String) {
        viewModelScope.launch {
            var token = userUiState.value.currentUser?.getIdToken(false)?.await()?.token
            while (token == null) {
                delay(1500L)
                token = userUiState.value.currentUser?.getIdToken(false)?.await()?.token
            }
            apolloClient.query(GetProfileQuery(id))
                .addHttpHeader("Authorization", "Bearer $token")
                .toFlow()
                .collect {
                    val response = it
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
                            _userUiState.update { uiState ->
                                uiState.copy(userInfo = response.data)
                            }
                        }
                        else -> {
                            // Another fetch error, maybe a cache miss?
                            // Or potentially a non-compliant server returning data: null without an error
                            Log.d("Another fetch error", "Oh no... An error happened.")
                        }
                    }
                }
        }
    }

    suspend fun updateHandle(mainActivity: MainActivity, handle: String) {
        if (handle == userUiState.value.userInfo?.profile?.userProfile?.handle.toString()) return
        var isTaken = false;
        viewModelScope.launch() {
            if (handle.length !in 3..22) {
                Toast.makeText(
                    mainActivity.baseContext,
                    "Invalid handle. It must be between 2 and 22 characters long.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            else {
                val result = apolloClient.query(GetProfileByHandleQuery(handle)).fetchPolicy(FetchPolicy.NetworkOnly).execute()
                if (result.data?.profile?.isEmpty() == false) {
                    isTaken = true
                    Toast.makeText(
                        mainActivity.baseContext,
                        "Sorry, this handle has already been taken.",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    isTaken = false
                }
                if (!isTaken) {
                    val token = userUiState.value.currentUser?.getIdToken(false)?.await()?.token
                    val response = apolloClient.mutation(UpdateHandleMutation(
                        userUiState.value.currentUser!!.uid,
                        handle.trim(),
                        normalizeHandle(handle))
                    ).addHttpHeader("Authorization", "Bearer $token")
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
                            getUserInfo(userUiState.value.currentUser!!.uid)
                        }
                        else -> {
                            // Another fetch error, maybe a cache miss?
                            // Or potentially a non-compliant server returning data: null without an error
                            Log.d("Another fetch error", "Oh no... An error happened.")
                        }
                    }
                }
            }
        }.join()
    }
}

data class UserUIState(
    val currentUser: FirebaseUser? = Firebase.auth.currentUser,
    val userInfo: GetProfileQuery.Data? = null,
)