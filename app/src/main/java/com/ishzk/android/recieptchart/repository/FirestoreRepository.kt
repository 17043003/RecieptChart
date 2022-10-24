package com.ishzk.android.recieptchart.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FirestoreRepository: HouseholdRepository {
    private val db by lazy { Firebase.firestore }

    override fun addItem(item: Household){

        val itemCollection = db.collection("users")
            .document(item.userId)
            .collection("items")

        if(item.id.isNotEmpty()) { // when edit item.
            itemCollection
                .document(item.id)
                .set(item)
        }else{ // when create new item.
            itemCollection.add(item)
        }
    }

    override suspend fun fetchItems(userID: String): List<Household> {
        val itemList = mutableListOf<Household>()

        if(userID.isEmpty()) return itemList

        return withContext(Dispatchers.Default) {

            val result = db.collection("users")
                .document(userID)
                .collection("items")
                .orderBy("date")
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

            while (!result.isComplete) {
            }

            result.result.documents.map {
                it.data ?: return@map

                val timeStamp = it.data?.get("date") as Timestamp

                val item = Household(
                    it.id,
                    it.data?.get("cost").toString().toIntOrNull() ?: 0,
                    timeStamp,
                    it.data?.get("kind").toString(),
                    "",
                    it.data?.get("userID").toString()
                )
                itemList.add(item)
            }
            itemList
        }
    }

    override suspend fun fetchPeriodicItems(
        userID: String,
        start: Date,
        end: Date
    ): List<Household> {
        val itemList = mutableListOf<Household>()

        if(userID.isEmpty()) return itemList

        return withContext(Dispatchers.Default) {
            val result = db.collection("users")
                .document(userID)
                .collection("items")
                .orderBy("date")
                .startAt(Timestamp(start))
                .endAt(Timestamp(end))
                .get()

            while (!result.isComplete) {
            }
            result.result.documents.map {
                it.data ?: return@map

                val timeStamp = it.data?.get("date") as Timestamp

                val item = Household(
                    it.id,
                    it.data?.get("cost").toString().toIntOrNull() ?: 0,
                    timeStamp,
                    it.data?.get("kind").toString(),
                    "",
                    it.data?.get("userID").toString()
                )
                itemList.add(item)
            }
            itemList
        }
    }

    override suspend fun fetchItem(userID: String, itemID: String): Household? {
        return withContext(Dispatchers.Default) {
            val result = db.collection("users")
                .document(userID)
                .collection("items")
                .document(itemID)
                .get()

            while (!result.isComplete) {
            }

            result.result.data?.let {

                val timeStamp = it.get("date") as Timestamp

                Household(
                    itemID,
                    it.get("cost").toString().toIntOrNull() ?: 0,
                    timeStamp,
                    it.get("kind").toString(),
                    "",
                    it.get("userID").toString()
                )
            }
        }
    }

    companion object {
        const val TAG = "FirestoreRepository"
    }
}