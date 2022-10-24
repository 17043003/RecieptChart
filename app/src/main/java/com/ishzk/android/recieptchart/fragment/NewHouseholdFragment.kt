package com.ishzk.android.recieptchart.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentNewHouseholdBinding
import com.ishzk.android.recieptchart.model.ItemKind
import com.ishzk.android.recieptchart.viewmodel.NewHouseholdViewModel
import com.ishzk.android.recieptchart.viewmodel.toLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate

class NewHouseholdFragment: Fragment() {
    private var _binding: FragmentNewHouseholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewHouseholdViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewHouseholdBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.userID = SharedPreference(requireActivity())
            .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))

        // edit item values from db.
        lifecycleScope.launch {
            val args: NewHouseholdFragmentArgs by navArgs()
            val itemID = args.itemID ?: return@launch // when create new item, skip under process.
            val item = viewModel.fetchEditItem(itemID)

            viewModel.itemID.value = item.id
            viewModel.cost.value = item.cost
            viewModel.selectedDate.value = item.date.toDate().toLocalDate()
            viewModel.selectedKindPosition.value = ItemKind.values().indexOf(ItemKind.valueOf(item.kind))
            viewModel.detail.value = item.description
        }


        // set date picker listener to date edit.
        val today = LocalDate.now()
        viewModel.selectedDate.value = today
        binding.editItemDate.setOnClickListener {
            DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, y, m, d ->
                viewModel.selectedDate.value = LocalDate.of(y, m + 1, d)
            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasAddedItem.collect {
                    if (it) view!!.findNavController().navigate(
                            NewHouseholdFragmentDirections.actionNewHouseholdFragmentToHouseholdFragment()
                    )
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}