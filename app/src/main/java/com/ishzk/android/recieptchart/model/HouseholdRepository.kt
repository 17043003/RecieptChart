package com.ishzk.android.recieptchart.model

interface HouseholdRepository {
    fun addItem(item: Household)
    suspend fun fetchItems(userID: String): List<Household>
}