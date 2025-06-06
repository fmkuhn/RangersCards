package com.rangerscards

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var isDataLoaded = false
        splashScreen.setKeepOnScreenCondition { !isDataLoaded }
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            viewModel = viewModel(factory = AppViewModelProvider.Factory)
            // Collecting user's theme from shared preferences via viewmodel - false = light, true = dark
            val currentTheme = when(viewModel.themeState.collectAsState().value) {
                0 -> false
                1 -> true
                2 -> isSystemInDarkTheme()
                else -> null
            }
            if (currentTheme != null) {
                isDataLoaded = true
                RangersCardsTheme(currentTheme) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = CustomTheme.colors.l30
                    ) {
                        RangersApp(this, currentTheme, viewModel)
                    }
                }
            }
        }
    }

    fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    viewModel.setUser(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    authenticationFailedToast()
                    viewModel.setUser(null)
                }
            }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    viewModel.setUser(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    authenticationFailedToast()
                    viewModel.setUser(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun authenticationFailedToast() {
        Toast.makeText(
            baseContext,
            getString(R.string.authentication_failed_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }
}