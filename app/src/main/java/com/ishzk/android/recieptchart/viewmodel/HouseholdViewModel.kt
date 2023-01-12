package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishzk.android.recieptchart.model.FetchMonthlyHouseholdsEachDayUseCase
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HouseholdViewModel: ViewModel() {
    var _repository: HouseholdRepository? = null
    private val repository by lazy { _repository!! }
    var _userID = ""
    val fetchedItems = MutableLiveData(listOf<Household>())
    val isFetching by lazy { MutableLiveData(false) }

    suspend fun fetchItems() = withContext(Dispatchers.Default) {
        val userID = _userID

        isFetching.postValue(true)
        val items = repository.fetchItems(userID)
        isFetching.postValue(false)

        fetchedItems.postValue(items)
    }

    suspend fun fetchBetweenItems(start: Date, end: Date): Flow<List<Household>> = withContext(Dispatchers.Default) {
        flow {
            val userID = _userID

            isFetching.postValue(true)
            val items = repository.fetchPeriodicItems(userID, start, end)
            isFetching.postValue(false)

            fetchedItems.postValue(items)
        }
    }

    suspend fun fetchTodayItems(today: Date): Flow<List<Household>> = withContext(Dispatchers.Default) {
        val start = today.beginDay()
        val end = today.endDay()
        fetchBetweenItems(start, end)
    }

    suspend fun fetchMonthlyItems(today: Date) = withContext(Dispatchers.Default) {
        val userID = _userID
        val usecase = FetchMonthlyHouseholdsEachDayUseCase()
        usecase.repository = repository

        isFetching.postValue(true)
        val items = usecase(userID, today)
        isFetching.postValue(false)

        fetchedItems.postValue(items)
    }

    fun deleteItem(id: String){
        val userID = _userID
        viewModelScope.launch {
            val result = repository.deleteItem(userID, id)
            if(result) {
                val list = fetchedItems.value?.toMutableList() ?: mutableListOf()
                list.removeIf{ it.id == id }
                fetchedItems.postValue(list)
            }
        }
    }
}

fun Date.beginDay(): Date = Date(year, month, date, 0, 0)
fun Date.endDay(): Date = Date(year, month, date, 23, 59)