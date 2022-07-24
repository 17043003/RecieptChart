package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class MainViewModel: ViewModel() {
    var currentUser: FirebaseUser? = null
}