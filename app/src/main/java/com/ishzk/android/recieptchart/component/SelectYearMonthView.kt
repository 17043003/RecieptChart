package com.ishzk.android.recieptchart.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.ishzk.android.recieptchart.databinding.ViewSelectyearmonthBinding
import java.util.Date

/**
 * To select year and month, show selected date.
 */
class SelectYearMonthView : LinearLayout {
    private val binding by lazy { ViewSelectyearmonthBinding.inflate(LayoutInflater.from(context), this, true) }
    val selectedYear by lazy { MutableLiveData<Int>() }
    val selectedMonth by lazy { MutableLiveData<Int>() }
    val selectedDate: Date
        get() = Date((selectedYear.value ?: 2022) - 1900, (selectedMonth.value ?: 1) - 1, 1)

    init {
        binding.nextMonthButton.setOnClickListener{
            val currentMonth = selectedMonth.value ?: 1
            val currentYear = selectedYear.value ?: 2022

            selectedMonth.value = if(currentMonth == 12) 1 else currentMonth + 1
            selectedYear.value = if(currentMonth == 12) currentYear + 1 else currentYear

            binding.selectedYearMonthText.text = "${selectedYear.value}/${(selectedMonth.value ?: 1)}"
        }

        binding.previousMonthButton.setOnClickListener {
            val currentMonth = selectedMonth.value ?: 1
            val currentYear = selectedYear.value ?: 2022

            selectedMonth.value = if(currentMonth == 1) 12 else currentMonth - 1
            selectedYear.value = if(currentMonth == 1) currentYear - 1 else currentYear

            binding.selectedYearMonthText.text = "${selectedYear.value}/${(selectedMonth.value ?: 1)}"
        }

        binding
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        binding.selectedYearMonthText.text = "${selectedYear.value}/${(selectedMonth.value ?: 1)}"
    }
}