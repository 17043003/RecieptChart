package com.ishzk.android.recieptchart.model

import java.util.*

interface HouseholdRepository {
    fun addItem(item: Household)
    suspend fun fetchItems(userID: String): List<Household>
    suspend fun fetchPeriodicItems(userID: String, start: Date, end: Date): List<Household>
    suspend fun fetchItem(userID: String, itemID: String): Household?
}