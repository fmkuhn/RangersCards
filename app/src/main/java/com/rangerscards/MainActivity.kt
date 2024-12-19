package com.rangerscards

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            viewModel = viewModel(factory = AppViewModelProvider.Factory)
            // Collecting user's theme from shared preferences via viewmodel - false = light, true = dark
            // var currentTheme by viewmodel.getTheme().collectAsState(null)
            var currentTheme by remember { mutableStateOf(false) }
            RangersCardsTheme(/*currentTheme*/) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CustomTheme.colors.l30
                ) {
                    RangersApp(this, isSystemInDarkTheme())
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
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
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
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    viewModel.setUser(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}