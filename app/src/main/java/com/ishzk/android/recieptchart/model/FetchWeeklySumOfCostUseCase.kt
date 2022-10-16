package com.ishzk.android.recieptchart.model

import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.viewmodel.beginDayOfWeek
import com.ishzk.android.recieptchart.viewmodel.daysOfWeek
import com.ishzk.android.recieptchart.viewmodel.endDay
import com.ishzk.android.recieptchart.viewmodel.endDayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class FetchWeeklySumOfCostUseCase {
    lateinit var repository: HouseholdRepository

    suspend operator fun invoke(userID: String, date: Date): List<Int> = withContext(Dispatchers.Default) {
        val items = repository.fetchPeriodicItems(
            userID,
            date.beginDayOfWeek(),
            date.endDayOfWeek().endDay()
        )

        val sumEachDay = items.groupBy { it.date }.map {
            it.key to it.value.sumOf { it.cost }
        }.toMap()

        val days = date.daysOfWeek()

        (0..6).map { i ->
            if (Timestamp(days[i]) in sumEachDay) sumEachDay[Timestamp(days[i])] ?: 0
            else 0
        }
    }
}