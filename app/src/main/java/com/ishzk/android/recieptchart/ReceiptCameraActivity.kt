package com.ishzk.android.recieptchart

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import com.github.michaelbull.result.getOrElse
import com.google.gson.Gson
import com.ishzk.android.recieptchart.databinding.ActivityCameraBinding
import com.ishzk.android.recieptchart.model.CapturedReceiptData
import com.ishzk.android.recieptchart.viewmodel.ReceiptCameraViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ReceiptCameraActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private val viewModel: ReceiptCameraViewModel by viewModels()
    private val characterRecognition = CharacterRecognition()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this

        // After capture button clicked.
        viewModel.cameraOutputOptions.observe(this){
            val imageCapture = imageCapture ?: return@observe
            imageCapture.takePicture(
                it,
                ContextCompat.getMainExecutor(this),
                viewModel.saveImageCallback
            )
        }

        // After succeeded save image.
        viewModel.takenPictureUri.observe(this){
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            val scaledBitmap = characterRecognition.scaleBitmapDown(bitmap, 640)
            val base64Bitmap = characterRecognition.encode2Base64(scaledBitmap)
            val request = characterRecognition.createRequest(base64Bitmap)
            lifecycleScope.launch {
                val result = characterRecognition.recognition(request)
                viewModel.isTakingPicture.postValue(false)

                val receiptData: CapturedReceiptData = result.getOrElse { return@launch }
                Log.d(TAG, receiptData.toString())
                if(receiptData.costs.isEmpty()) return@launch

                // Start activity to save captured receipt data.
                val intent = Intent(applicationContext, ReceiptRegisterActivity::class.java)
                val gson = Gson()
                val data = gson.toJson(receiptData) ?: return@launch
                intent.putExtra("CaptureDataString", data)
                startActivity(intent)
            }
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if(result.resultCode != RESULT_OK) return@registerForActivityResult

            result.data?.data?.also {
                viewModel.isTakingPicture.postValue(true)
                viewModel.takenPictureUri.postValue(it)
            }
        }
        val pickPhotoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
        }

        viewModel.pushedPickButton.observe(this){
            if(it) {
                launcher.launch(pickPhotoIntent)
            }
        }

        binding.viewModel = viewModel

        if(allPermissionsGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()

            // select back camera as default.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }catch (e: Exception){
                Log.e(TAG, "Use case binding failed.", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera()
            }else{
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReceiptCameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}

val AndroidViewModel.context: Context
  get() = getApplication()
