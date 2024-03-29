package com.ishzk.android.recieptchart

import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.Converter
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import com.ishzk.android.recieptchart.viewmodel.HouseholdViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDateTime
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class HouseholdUnitTest {
    @Test
    fun toStringAndInverse(){
        val cost = 1234
        val stringCost = Converter.toString(cost)
        assertEquals(stringCost, "1234")

        val number = Converter.inverseToInt("1234")
        assertEquals(cost, number)
    }

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun fetchItemsNormalTest(){
        val item = Household("1", 1234, Timestamp(Date()), "Consume", "", "me")
        val viewModel = HouseholdViewModel()
        val repository = object: HouseholdRepository {
            override fun addItem(item: Household) {}

            override suspend fun fetchItems(userID: String): List<Household> =
                listOf(item)

            override suspend fun fetchPeriodicItems(
                userID: String,
                start: Date,
                end: Date
            ): List<Household> {
                return listOf(item)
            }
        }
        viewModel._repository = repository
        runTest {
            viewModel.fetchItems().collect{
                assertEquals(it.first(), item)
            }
        }
    }
}