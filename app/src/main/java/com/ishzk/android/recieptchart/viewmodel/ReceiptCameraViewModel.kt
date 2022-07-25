package com.ishzk.android.recieptchart.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ishzk.android.recieptchart.context
import java.text.SimpleDateFormat
import java.util.*

const val TAG = "ReceiptCameraViewModel"

class ReceiptCameraViewModel(application: Application) : AndroidViewModel(application) {
    val cameraOutputOptions = MutableLiveData<ImageCapture.OutputFileOptions>()
    val takenPictureUri = MutableLiveData<Uri>()

    fun captureReceipt(){
        Log.d(TAG, "Capture button is touched.")
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Receipt-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
        cameraOutputOptions.value = outputOptions
    }

    fun pickImageFile(){
        Log.d(TAG, "Select image floating button is pushed.")
    }

    val saveImageCallback = object : ImageCapture.OnImageSavedCallback{
        override fun onError(exception: ImageCaptureException) {
            Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
            Log.d(TAG, msg)

            takenPictureUri.value = outputFileResults.savedUri
        }
    }

    companion object{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}