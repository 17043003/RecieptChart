package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import com.ishzk.android.recieptchart.model.ItemKind
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class NewHouseholdViewModel: ViewModel() {
    val repository: HouseholdRepository by lazy { FirestoreRepository() }

    val itemID by lazy { MutableLiveData<String>("") }
    val cost by lazy { MutableLiveData<Int?>() }
    val kinds: List<String> = ItemKind.values().map { it.kind }
    val selectedKindPosition by lazy { MutableLiveData<Int>() }
    val selectedDate by lazy { MutableLiveData<LocalDate>() }
    val detail by lazy { MutableLiveData<String>() }
    private val _hasAddedItem = MutableStateFlow(false)
    val hasAddedItem = _hasAddedItem.asStateFlow()

    var userID: String = ""

    fun selectedDateString(date: LocalDate): String{
        return "${date.year}/${date.monthValue}/${date.dayOfMonth}"
    }

    fun createItem(){
        val currentLocalDate = selectedDate.value ?: LocalDate.now()
        val currentDate = Date.from(currentLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val timeStamp = Timestamp(currentDate)
        val itemID = itemID.value
        repository.addItem(Household(itemID ?: "", cost.value ?: 0,
            timeStamp,
            ItemKind.values()[selectedKindPosition.value ?: 0].kind,
            detail.value ?: "",
            userID
        ))

        _hasAddedItem.value = true
    }

    suspend fun fetchEditItem(itemID: String): Household{
        return withContext(Dispatchers.Default) {
            repository.fetchItem(userID, itemID)
        } ?: Household.init()
    }
}