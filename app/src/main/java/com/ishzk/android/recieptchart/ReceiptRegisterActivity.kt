package com.ishzk.android.recieptchart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val adapter = RegisterItemAdapter()

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

        // attach captured receipt data list to recycler view.
        binding.registerItemRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onStart() {
        super.onStart()

        adapter.submitList(viewModel.receiptData.value?.costs)
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val TAG = "ReceiptRegisterActivity"
    }
}

class RegisterItemAdapter: ListAdapter<CapturedCost, RegisterItemListViewHolder>(CALL_BACK){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisterItemListViewHolder {
        val view = ItemRegisterRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegisterItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegisterItemListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class RegisterItemListViewHolder(private val binding: ItemRegisterRecyclerviewBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CapturedCost){
        binding.receiptItem = item
    }
}

val CALL_BACK = object : DiffUtil.ItemCallback<CapturedCost>() {
    override fun areContentsTheSame(
        oldItem: CapturedCost,
        newItem: CapturedCost
    ): Boolean {
//        return oldItem == newItem
        return false
    }

    override fun areItemsTheSame(
        oldItem: CapturedCost,
        newItem: CapturedCost
    ): Boolean {
//        return oldItem == newItem
        return false
    }
}