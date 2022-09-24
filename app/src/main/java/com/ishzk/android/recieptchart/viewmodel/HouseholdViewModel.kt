package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class HouseholdViewModel: ViewModel() {
    var _repository: HouseholdRepository? = null
    private val repository by lazy { _repository!! }
    var _userID = ""

    suspend fun fetchItems(): Flow<List<Household>> = withContext(Dispatchers.Default) {
        flow {
            val userID = _userID
            val items = repository.fetchItems(userID)
            emit(items)
        }
    }
}