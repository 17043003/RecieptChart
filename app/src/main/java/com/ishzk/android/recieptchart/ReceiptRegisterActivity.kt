package com.ishzk.android.recieptchart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ishzk.android.recieptchart.databinding.ActivityReceiptRegisterBinding
import com.ishzk.android.recieptchart.databinding.ItemRegisterRecyclerviewBinding
import com.ishzk.android.recieptchart.model.CapturedCost
import com.ishzk.android.recieptchart.model.CapturedReceiptData
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import com.ishzk.android.recieptchart.viewmodel.ReceiptRegisterViewModel

class ReceiptRegisterActivity: AppCompatActivity() {
    val viewModel: ReceiptRegisterViewModel by viewModels()
    private lateinit var binding: ActivityReceiptRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReceiptRegisterBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        val extras = intent.extras
        if(extras != null){
            val captureDataString = extras["CaptureDataString"].toString()
            val gson = Gson()
            val capturedData = gson.fromJson(captureDataString, CapturedReceiptData::class.java)
            Log.d(TAG, captureDataString)

            viewModel.repository = FirestoreRepository()
            viewModel.receiptData.value = capturedData
            viewModel.userId = SharedPreference(this)
                .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))
        }

        binding.registerItemRecyclerView
    }

    companion object {
        const val TAG = "ReceiptRegisterActivity"
    }
}
