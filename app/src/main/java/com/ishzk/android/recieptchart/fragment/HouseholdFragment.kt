package com.ishzk.android.recieptchart.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ishzk.android.recieptchart.databinding.FragmentHouseholdBinding

class HouseholdFragment: Fragment() {
    private var _binding: FragmentHouseholdBinding? = null
    private val binding get() = _binding!!

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

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}