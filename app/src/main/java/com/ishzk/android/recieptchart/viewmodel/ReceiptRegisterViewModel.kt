package com.ishzk.android.recieptchart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReceiptRegisterViewModel: ViewModel() {
    lateinit var repository: HouseholdRepository
    val registerDate = MutableLiveData<Date>()
    val registerCosts = MutableLiveData<List<CapturedCost>>()

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _isSaved = MutableLiveData(false)
    val isSaved:LiveData<Boolean> = _isSaved

    private val _isCanceled = MutableLiveData<Boolean>(false)
    val isCanceled: LiveData<Boolean> = _isCanceled

    private val _willBeEdited = MutableLiveData<CapturedCost>()
    val willBeEdited: LiveData<CapturedCost> = _willBeEdited

    private val _editedItem = MutableLiveData<CapturedCost>()
    val editedItem: LiveData<CapturedCost> = _editedItem

    var userId: String = ""

    fun saveHouseholds(){
        Log.d(TAG, "save button is clicked.")
        if(userId.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _isSaving.postValue(true)

                val date = registerDate.value ?: Date()
                registerCosts.value?.forEach { data ->
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

    fun clickedItemToEdit(item: CapturedCost){
        val selectedItem = registerCosts.value?.find { it.id == item.id } ?: return
        _willBeEdited.postValue(selectedItem)
    }

    fun updateItem(item: CapturedCost){
        val editedList = registerCosts.value?.map {
            if(it.id == item.id){
                CapturedCost(
                    item.id,
                    item.cost,
                    item.description
                )
            }else{
                it
            }
        } ?: listOf()

        registerCosts.postValue(editedList)
    }

    fun removeItem(item: CapturedCost){
        val mutableList = registerCosts.value?.toMutableList() ?: mutableListOf()
        mutableList.remove(item)
        registerCosts.postValue(mutableList)
    }

    companion object {
        const val TAG = "ReceiptRegisterViewModel"
    }
}