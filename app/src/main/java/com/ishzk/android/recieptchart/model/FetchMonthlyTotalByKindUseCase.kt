package com.ishzk.android.recieptchart.model

import com.ishzk.android.recieptchart.viewmodel.endDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FetchMonthlyTotalByKindUseCase {
    lateinit var repository: HouseholdRepository

    suspend operator fun invoke(userID: String, date: Date): Map<String, Int> = withContext(
        Dispatchers.Default) {
        val itemList = repository.fetchPeriodicItems(
            userID,
            date.beginDayOfMonth(),
            date.endDayOfMonth().endDay()
        )

        itemList.groupingBy { it.kind }.fold(0){ acc, e -> acc + e.cost}

    }
}