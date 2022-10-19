package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.FetchMonthlyHouseholdsEachDayUseCase
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*

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

    suspend fun fetchBetweenItems(start: Date, end: Date): Flow<List<Household>> = withContext(Dispatchers.Default) {
        flow {
            val userID = _userID
            val items = repository.fetchPeriodicItems(userID, start, end)
            emit(items)
        }
    }

    suspend fun fetchTodayItems(today: Date): Flow<List<Household>> = withContext(Dispatchers.Default) {
        val start = today.beginDay()
        val end = today.endDay()
        fetchBetweenItems(start, end)
    }

    suspend fun fetchMonthlyItems(today: Date): Flow<List<Household>> = withContext(Dispatchers.Default) {
        flow {
            val userID = _userID
            val usecase = FetchMonthlyHouseholdsEachDayUseCase()
            usecase.repository = repository
            val items = usecase(userID, today)
            emit(items)
        }
    }
}

fun Date.beginDay(): Date = Date(year, month, date, 0, 0)
fun Date.endDay(): Date = Date(year, month, date, 23, 59)