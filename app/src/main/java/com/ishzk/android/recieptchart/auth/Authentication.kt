package com.ishzk.android.recieptchart.auth

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Authentication {
    fun createIntent(): Intent{
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        // Create sign-in intent
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult): FirebaseUser? {
        val response = result.idpResponse
        return if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser
        } else {
            val errorMessage = response?.error?.message ?: ""
            val errorCode = response?.error?.errorCode ?: 0
            Log.v(TAG, "$errorCode:$errorMessage")
            null
        }
    }

    companion object {
        const val TAG = "FirebaseAuth"
    }
}