package com.ishzk.android.recieptchart.repository

import com.ishzk.android.recieptchart.model.Household

interface HouseholdRepository {
    fun addItem(item: Household)
    suspend fun fetchItems(userID: String): List<Household>
}