package com.ishzk.android.recieptchart.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentNewHouseholdBinding
import com.ishzk.android.recieptchart.viewmodel.MainViewModel
import com.ishzk.android.recieptchart.viewmodel.NewHouseholdViewModel
import java.time.LocalDate

class NewHouseholdFragment: Fragment() {
    private var _binding: FragmentNewHouseholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewHouseholdViewModel by viewModels()

    private val mainViewModel by viewModels<MainViewModel>()

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

        // set date picker listener to date edit.
        val today = LocalDate.now()
        viewModel.selectedDate.value = today
        binding.editItemDate.setOnClickListener {
            DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, y, m, d ->
                viewModel.selectedDate.value = LocalDate.of(y, m, d)
            }, today.year, today.monthValue, today.dayOfMonth).show()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}