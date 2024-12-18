package com.rangerscards.ui.settings

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.okHttpClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.GetProfileQuery
import com.rangerscards.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel(private var apolloClient: ApolloClient) : ViewModel() {

    private val _currentUser = MutableStateFlow(Firebase.auth.currentUser)
    var currentUser = _currentUser.asStateFlow()

    init {
        _currentUser.value?.let { createNewApolloClient(it) }
    }

    fun setUser(user: FirebaseUser?) {
        _currentUser.update { user }
    }

    fun signIn(mainActivity: MainActivity, email: String, password: String) {
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.signIn(email, password)
                _currentUser.value?.let { createNewApolloClient(it) }
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
                _currentUser.value?.let { createNewApolloClient(it) }
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
        removeInterceptorFromApolloClient()
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }

    private fun createNewApolloClient(firebaseUser: FirebaseUser) {
        apolloClient.close()
        apolloClient = apolloClient.newBuilder()
            .apply {
                okHttpClient(
                    OkHttpClient.Builder()
                        .addInterceptor(AuthorizationInterceptor(firebaseUser))
                        .build()
                )
            }
//            .serverUrl("https://gapi.rangersdb.com/v1/graphqlTest")
//            .okHttpClient(
//            OkHttpClient.Builder()
//                .addInterceptor(AuthorizationInterceptor(firebaseUser))
//                .build()
//            )
            .build()
    }

    private fun removeInterceptorFromApolloClient() {
        apolloClient.newBuilder().removeInterceptor(apolloClient.interceptors[apolloClient.interceptors.lastIndex])
    }

    fun getUserInfo(id: String): GetProfileQuery.Data? {
        var result: GetProfileQuery.Data? = null
        viewModelScope.launch {
            val response = apolloClient.query(GetProfileQuery(id)).execute()
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
        }
        return result
    }
}

private class AuthorizationInterceptor(val user: FirebaseUser) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                addHeader("Authorization", "Bearer ${user.getIdToken(true)}")
            }
            .build()
        return chain.proceed(request)
    }
}