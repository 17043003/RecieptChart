package com.ishzk.android.recieptchart.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishzk.android.recieptchart.model.Household

class FirestoreRepository {
    private val db by lazy { Firebase.firestore }

    fun addItem(item: Household){

        db.collection("users")
            .document(item.userId)
            .collection("items")
            .add(item)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    companion object {
        const val TAG = "FirestoreRepository"
    }
}