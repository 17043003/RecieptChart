package com.ishzk.android.recieptchart.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishzk.android.recieptchart.model.Household
import java.time.LocalDateTime

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

    fun fetchItems(userID: String): List<Household> {
        val itemList = mutableListOf<Household>()

        if(userID.isEmpty()) return itemList

        val result = db.collection("users")
            .document(userID)
            .collection("items")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.documents}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        result.result.documents.map {
            it.data ?: return@map

            val item = Household(
                it.id,
                it.data?.get("cost").toString().toIntOrNull() ?: 0,
                LocalDateTime.now(),
                it.data?.get("kind").toString(),
                "",
                it.data?.get("userID").toString()
            )

            if(item != null) {
                itemList.add(item)
            }
        }

        return itemList
    }

    companion object {
        const val TAG = "FirestoreRepository"
    }
}