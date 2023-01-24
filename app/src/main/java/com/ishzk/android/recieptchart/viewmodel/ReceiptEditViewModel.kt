package com.ishzk.android.recieptchart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ishzk.android.recieptchart.model.CapturedCost

class ReceiptEditViewModel: ViewModel() {
    private var id = 0

    val cost = MutableLiveData<Int>(0)
    val description = MutableLiveData<String>("")

    private val _editItem = MutableLiveData<CapturedCost>()
    val editItem: LiveData<CapturedCost> = _editItem

    fun setEditItem(itemId: Int, itemCost: Int, itemDescription: String){
        id = itemId
        cost.value = itemCost
        description.value = itemDescription
    }

    fun onClickedSave(){
        val editedItem = CapturedCost(id, cost.value ?: 0, description.value ?: "")
        _editItem.postValue(editedItem)
    }
}