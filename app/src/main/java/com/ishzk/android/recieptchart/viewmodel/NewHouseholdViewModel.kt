package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.ItemKind
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class NewHouseholdViewModel: ViewModel() {
    val cost by lazy { MutableLiveData<Int?>() }
    val kinds: List<String> = ItemKind.values().map { it.kind }
    val selectedKindPosition by lazy { MutableLiveData<Int>() }
    val selectedDate by lazy { MutableLiveData<LocalDate>() }
    private val _hasAddedItem = MutableStateFlow(false)
    val hasAddedItem = _hasAddedItem.asStateFlow()

    var userID: String = ""

    fun selectedDateString(date: LocalDate): String{
        return "${date.year}/${date.monthValue}/${date.dayOfMonth}"
    }

    fun createItem(){
        val repository = FirestoreRepository()
        val currentLocalDate = selectedDate.value ?: LocalDate.now()
        val currentDate = Date.from(currentLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val timeStamp = Timestamp(currentDate)
        repository.addItem(Household("", cost.value ?: 0,
            timeStamp,
            ItemKind.values()[selectedKindPosition.value ?: 0].kind,
            "",
            userID
        ))

        _hasAddedItem.value = true
    }
}