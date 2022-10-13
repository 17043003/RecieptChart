package com.ishzk.android.recieptchart

import android.graphics.Color
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

class BarChartData {
    fun prepareData(x: List<Float>,y: List<Float>): List<BarEntry> {
        // prepare Entry.
        return x.zip(y).map {
            BarEntry(it.first, floatArrayOf(it.second))
        }
    }

    fun prepareDataset(entryList: List<BarEntry>): BarData{
        val barDataSets = mutableListOf<IBarDataSet>()
        val barDataSet = BarDataSet(entryList, "linear")
        barDataSet.colors = listOf(Color.BLUE, Color.RED)
        barDataSets.add(barDataSet)

        return BarData(barDataSets)
    }
}