package com.ishzk.android.recieptchart.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentHouseholdBinding
import com.ishzk.android.recieptchart.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

class HouseholdFragment: Fragment() {
    private var _binding: FragmentHouseholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HouseholdViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHouseholdBinding.inflate(layoutInflater, container, false)

        binding.newHouseholdButton.setOnClickListener { view ->
            view.findNavController().
            navigate(HouseholdFragmentDirections.actionHouseholdFragmentToNewHouseholdFragment())
        }

        viewModel._userID = SharedPreference(requireActivity())
            .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchItems().collect{
                Log.d(TAG, "${it.size}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    companion object {
        const val TAG = "HouseholdFragment"
    }
}