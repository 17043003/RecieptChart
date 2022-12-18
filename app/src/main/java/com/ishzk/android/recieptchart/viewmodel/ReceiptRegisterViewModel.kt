package com.ishzk.android.recieptchart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.CapturedReceiptData
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.model.HouseholdRepository
import com.ishzk.android.recieptchart.model.ItemKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReceiptRegisterViewModel: ViewModel() {
    lateinit var repository: HouseholdRepository
    val receiptData = MutableLiveData<CapturedReceiptData>()
    val registerDate = MutableLiveData<Date>()

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _isSaved = MutableLiveData(false)
    val isSaved:LiveData<Boolean> = _isSaved

    private val _isCanceled = MutableLiveData<Boolean>(false)
    val isCanceled: LiveData<Boolean> = _isCanceled

    var userId: String = ""

    fun saveHouseholds(){
        Log.d(TAG, "save button is clicked.")
        if(userId.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _isSaving.postValue(true)

                val date = registerDate.value ?: Date()
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

                _isSaving.postValue(false)
                _isSaved.postValue(true)
            }
        }
    }

    fun clickedCancel(){
        _isCanceled.postValue(true)
    }

    companion object {
        const val TAG = "ReceiptRegisterViewModel"
    }
}