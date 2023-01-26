package com.ishzk.android.recieptchart.model

import com.ishzk.android.recieptchart.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FetchMonthlyHouseholdsEachDayUseCase {
    lateinit var repository: HouseholdRepository

    suspend operator fun invoke(userID: String, date: Date): Map<Date, List<Household>> = withContext(Dispatchers.Default) {
        val items = repository.fetchPeriodicItems(
            userID,
            date.beginDayOfMonth(),
            date.endDayOfMonth().endDay()
        )

        items.groupBy { it.date.toDate().beginDay() }
    }
}

fun Date.beginDayOfMonth(): Date{
    val local = toLocalDate()
    val beginDay = local.minusDays(local.dayOfMonth.toLong() - 1)
    return beginDay.toDate()
}

fun Date.endDayOfMonth(): Date{
    val local = beginDayOfMonth().toLocalDate()
    val endDay = local.plusMonths(1).minusDays(1)

    return endDay.toDate()
}