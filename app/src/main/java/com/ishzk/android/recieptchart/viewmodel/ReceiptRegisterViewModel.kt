package com.ishzk.android.recieptchart.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.CapturedReceiptData
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import com.ishzk.android.recieptchart.model.ItemKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReceiptRegisterViewModel: ViewModel() {
    lateinit var repository: HouseholdRepository
    val receiptData = MutableLiveData<CapturedReceiptData>()
    val isSaving = MutableStateFlow(false)
    var userId: String = ""

    fun saveHouseholds(){
        Log.d(TAG, "save button is clicked.")
        if(userId.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                isSaving.emit(true)

                val date = receiptData.value?.date ?: Date()
                receiptData.value?.costs?.forEach { data ->
                    val item = Household(
                        "",
                        data.cost,
                        Timestamp(date),
                        ItemKind.Other.kind,
                        data.description,
                        userId
                    )
                    repository.addItem(item)
                }
                isSaving.emit(false)
            }
        }
    }

    companion object {
        const val TAG = "ReceiptRegisterViewModel"
    }
}