package com.ishzk.android.recieptchart.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.ishzk.android.recieptchart.BarChartData
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentChartBinding
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import com.ishzk.android.recieptchart.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val today = Date()
            viewModel.fetchWeekItems(today).collect{
                val sumEachDay = it.groupBy { it.date }.map {
                    it.key to it.value.sumOf { it.cost }
                }.toMap()

                val days = today.daysOfWeek()
                val costs: List<Int> = (0..6).map { i ->
                    if(Timestamp(days[i]) in sumEachDay) sumEachDay[Timestamp(days[i])] ?: 0
                    else 0
                }

                val x = (0..6).toList().map { it.toFloat() } // X軸データ
                val chartData = BarChartData()
                val dataSet = chartData.prepareData(x, costs.map { cost -> cost.toFloat() })
                val barData = chartData.prepareDataset(dataSet)
                binding.dailyBarChart.apply {
                    data = barData
                    xAxis.isEnabled = true
                    xAxis.textColor = Color.BLACK
                    xAxis.valueFormatter = IndexAxisValueFormatter(
                        days.map { date ->
                            val sdf = SimpleDateFormat("MM/dd")
                            sdf.format(date)
                        })
                }
                binding.dailyBarChart.invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}