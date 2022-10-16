package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.FetchWeeklySumOfCostUseCase
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class ChartFragmentViewModel: ViewModel() {
    var _repository: HouseholdRepository? = null
    private val repository by lazy { _repository!! }
    var _userID = ""

    private suspend fun fetchBetweenItems(start: Date, end: Date): List<Household> = withContext(
        Dispatchers.Default) {
        val userID = _userID
        repository.fetchPeriodicItems(userID, start, end)
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

fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())