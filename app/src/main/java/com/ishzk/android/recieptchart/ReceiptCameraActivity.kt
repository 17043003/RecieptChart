package com.ishzk.android.recieptchart

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ishzk.android.recieptchart.databinding.ActivityCameraBinding
import com.ishzk.android.recieptchart.viewmodel.ReceiptCameraViewModel

class ReceiptCameraActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private val viewModel: ReceiptCameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
    }
}