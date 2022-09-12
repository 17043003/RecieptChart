package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HouseholdViewModel: ViewModel() {
    private val repository = FirestoreRepository()
    var _userID = ""

    fun fetchItems(): Flow<List<Household>>{
        return flow {
            val userID = _userID
            val items = repository.fetchItems(userID)
            emit(items)
        }
    }
}