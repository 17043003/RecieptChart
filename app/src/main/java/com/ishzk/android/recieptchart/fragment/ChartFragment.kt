package com.ishzk.android.recieptchart.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentChartBinding
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import com.ishzk.android.recieptchart.viewmodel.*
import kotlinx.coroutines.launch
import java.util.*

class ChartFragment: Fragment() {
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ChartFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChartBinding.inflate(inflater, container, false)

        binding.chartTitle.text = "Weekly chart"

        viewModel._repository = FirestoreRepository()
        viewModel._userID = SharedPreference(requireActivity())
            .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))

        // set initial date to select year month custom view.
        with(binding.selectYearMonth) {
            val today = Date()
            selectedYear.value = today.year + 1900
            selectedMonth.value = today.month + 1
            selectedDate.value = today
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            val today = Date()
            viewModel.fetchMonthlyItems(today)
            viewModel.fetchedTotals.observe(viewLifecycleOwner){ items ->
                val entries = items.map {
                    PieEntry(it.value.toFloat(), it.key)
                }

                val pieDataSet = PieDataSet(entries, "")
                with(pieDataSet){
                    colors = ColorTemplate.COLORFUL_COLORS.toList()
                    valueTextSize = 16f

                }

                val pieData = PieData(pieDataSet)
                with(binding.dailyPieChart){
                    data = pieData
                    centerText = "種別ごと一か月の合計"
                }
                binding.dailyPieChart.invalidate()
            }

            viewModel.total.observe(viewLifecycleOwner){ sum->
                with(binding.dailyPieChart){
                    centerText = "種別ごと一か月の合計\n${sum}円"
                }
            }
        }

        binding.selectYearMonth.selectedDate.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchMonthlyItems(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}