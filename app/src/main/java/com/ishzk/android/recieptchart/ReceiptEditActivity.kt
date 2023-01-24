package com.ishzk.android.recieptchart

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ishzk.android.recieptchart.databinding.ActivityReceiptEdtiBinding
import com.ishzk.android.recieptchart.model.CapturedCost
import com.ishzk.android.recieptchart.viewmodel.ReceiptEditViewModel

class ReceiptEditActivity: AppCompatActivity() {
    private lateinit var binding: ActivityReceiptEdtiBinding

    private val viewModel: ReceiptEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReceiptEdtiBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val id = intent.getIntExtra(ReceiptRegisterActivity.REQUEST_ID, 0)
        val cost = intent.getIntExtra(ReceiptRegisterActivity.REQUEST_COST, 0)
        val description = intent.getStringExtra(ReceiptRegisterActivity.REQUEST_DESCRIPTION) ?: ""
        viewModel.setEditItem(id, cost, description)

        setContentView(binding.root)

        lifecycleScope.launchWhenStarted {
            viewModel.editItem.observe(this@ReceiptEditActivity){
                val intent = Intent()
                val item = viewModel.editItem.value
                intent.putExtra(ReceiptRegisterActivity.RESPONSE_ID, item?.id)
                intent.putExtra(ReceiptRegisterActivity.RESPONSE_COST, item?.cost)
                intent.putExtra(ReceiptRegisterActivity.RESPONSE_DESCRIPTION, item?.description)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}