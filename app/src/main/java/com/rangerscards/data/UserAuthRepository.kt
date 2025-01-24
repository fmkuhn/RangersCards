package com.rangerscards.data

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.rangerscards.MainActivity
import com.rangerscards.R

class UserAuthRepository {
    fun invalidPasswordToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.invalid_password_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun invalidEmailToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.invalid_email_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun signIn(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.signIn(email, password)
            } else {
                invalidPasswordToast(context)
            }
        } else {
            invalidEmailToast(context)
        }
    }

    fun createAccount(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.createAccount(email, password)
            } else {
                invalidPasswordToast(context)
            }
        } else {
            invalidEmailToast(context)
        }
    }

    fun signOut(mainActivity: MainActivity) {
        mainActivity.signOut()
    }

    fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }
}