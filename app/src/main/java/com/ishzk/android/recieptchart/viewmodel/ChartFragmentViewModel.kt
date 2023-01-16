package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*

class ChartFragmentViewModel: ViewModel() {
    var _repository: HouseholdRepository? = null
    private val repository by lazy { _repository!! }
    var _userID = ""
    val fetchedTotals = MutableLiveData(mapOf<String, Int>())
    val total = MutableLiveData<Int>(0)

    private suspend fun fetchBetweenItems(start: Date, end: Date): List<Household> = withContext(
        Dispatchers.Default) {
        val userID = _userID
        repository.fetchPeriodicItems(userID, start, end)
    }

    suspend fun fetchMonthlyItems(today: Date) = withContext(Dispatchers.Default) {
        val userID = _userID
        val usecase = FetchMonthlyTotalByKindUseCase()
        usecase.repository = repository

        val items = usecase(userID, today)
        fetchedTotals.postValue(items)

        total.postValue(items.values.sum())
    }

    suspend fun fetchWeekItems(today: Date): Flow<List<Int>> = withContext(Dispatchers.Default) {
        val useCase = FetchWeeklySumOfCostUseCase()
        useCase.repository = repository
        flow {
            emit(useCase(_userID, today))
        }
    }
}

// return first day of this week.
fun Date.beginDayOfWeek(): Date {
    val localDate = this.toLocalDate()
    val firstDay = localDate.minusDays(localDate.dayOfWeek.value.toLong())
    return firstDay.toDate()
}

// return last day of this week.
fun Date.endDayOfWeek(): Date {
    val localDate = this.toLocalDate()
    val endDay = localDate.plusDays(6 - localDate.dayOfWeek.value.toLong())
    return endDay.toDate()
}

fun Date.daysOfWeek(): List<Date> {
    val firstLocalDate = this.beginDayOfWeek().toLocalDate()
    return (0..6).toList().map { firstLocalDate.plusDays(it.toLong()).toDate() }
}

fun Date.daysOfMonth(): List<Date> {
    val firstLocalDate = this.beginDayOfWeek().toLocalDate()
    val endLocalDate = firstLocalDate.with(TemporalAdjusters.lastDayOfMonth())
    return (0 until endLocalDate.dayOfMonth).toList().map { firstLocalDate.plusDays(it.toLong()).toDate() }
}

fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())