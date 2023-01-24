package com.ishzk.android.recieptchart

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.ishzk.android.recieptchart.viewmodel.toDate
import com.ishzk.android.recieptchart.viewmodel.toLocalDate
import java.time.LocalDate

class ReceiptRegisterActivity: AppCompatActivity() {
    val viewModel: ReceiptRegisterViewModel by viewModels()
    private lateinit var binding: ActivityReceiptRegisterBinding
    private val adapter by lazy { RegisterItemAdapter(viewModel) }

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
            viewModel.registerCosts.value = capturedData.costs
            viewModel.registerDate.value = capturedData.date

            viewModel.userId = SharedPreference(this)
                .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))
        }

        val decoration = object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val margin = 24
                outRect.set(margin / 2, 0, margin / 2, margin)
            }
        }

        // attach captured receipt data list to recycler view.
        binding.registerItemRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
            it.addItemDecoration(decoration)
        }

        // set date picker listener to date edit.
        val today = viewModel.registerDate.value?.toLocalDate() ?: LocalDate.now()
        binding.registerDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                viewModel.registerDate.postValue(LocalDate.of(y, m + 1, d).toDate())
            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isSaving.observe(this@ReceiptRegisterActivity) {
                if(it) {
                    // being saved, activity does not react.
                    window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            viewModel.isSaved.observe(this@ReceiptRegisterActivity) {
                if(it){
                    Toast.makeText(this@ReceiptRegisterActivity, "保存を完了しました", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            viewModel.isCanceled.observe(this@ReceiptRegisterActivity){
                if(it){
                    finish()
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            val launcher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ){ result ->
                if(result.resultCode == Activity.RESULT_OK){
                    val editedItem = result.data?.extras?.let {extra ->
                        CapturedCost(
                            extra.get(RESPONSE_ID).toString().toInt(),
                            extra.get(RESPONSE_COST).toString().toInt(),
                            extra.get(RESPONSE_DESCRIPTION).toString(),
                        )
                    } ?: return@registerForActivityResult

                    viewModel.updateItem(editedItem)
                }
            }

            viewModel.willBeEdited.observe(this@ReceiptRegisterActivity){
                // start edit item activity.
                val intent = Intent(application, ReceiptEditActivity::class.java)
                intent.putExtra(REQUEST_ID, it.id)
                intent.putExtra(REQUEST_COST, it.cost)
                intent.putExtra(REQUEST_DESCRIPTION, it.description)
                launcher.launch(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        adapter.submitList(viewModel.registerCosts.value)
        adapter.notifyDataSetChanged()

        lifecycleScope.launchWhenStarted {
            viewModel.registerCosts.observe(this@ReceiptRegisterActivity){
                adapter.submitList(viewModel.registerCosts.value)
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val TAG = "ReceiptRegisterActivity"
        const val REQUEST_ID = "ReceiptID"
        const val REQUEST_COST = "EditedCost"
        const val REQUEST_DESCRIPTION = "EditedDescription"

        const val RESPONSE_ID = "ReceiptID"
        const val RESPONSE_COST = "ReceiptCost"
        const val RESPONSE_DESCRIPTION = "ReceiptDescription"
    }
}

class RegisterItemAdapter(private val viewModel: ReceiptRegisterViewModel): ListAdapter<CapturedCost, RegisterItemListViewHolder>(CALL_BACK){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisterItemListViewHolder {
        val view = ItemRegisterRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegisterItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegisterItemListViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }
}

class RegisterItemListViewHolder(private val binding: ItemRegisterRecyclerviewBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CapturedCost, viewModel: ReceiptRegisterViewModel){
        binding.receiptItem = item
        binding.viewModel = viewModel
    }
}

val CALL_BACK = object : DiffUtil.ItemCallback<CapturedCost>() {
    override fun areContentsTheSame(
        oldItem: CapturedCost,
        newItem: CapturedCost
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: CapturedCost,
        newItem: CapturedCost
    ): Boolean {
        return oldItem.id == newItem.id
    }
}