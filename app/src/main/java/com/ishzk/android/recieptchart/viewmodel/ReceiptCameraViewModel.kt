package com.ishzk.android.recieptchart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel

const val TAG = "ReceiptCameraViewModel"

class ReceiptCameraViewModel: ViewModel() {
    fun captureReceipt(){
        Log.d(TAG, "Capture button is touched.")
    }

    fun pickImageFile(){
        Log.d(TAG, "Select image floating button is pushed.")
    }
}