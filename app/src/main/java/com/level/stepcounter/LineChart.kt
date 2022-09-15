package com.level.stepcounter
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LineChart {

    private val entries = ArrayList<Entry>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createchart(lineChart: LineChart, items:ArrayList<String>)
    {
       addEntries(items)

        val vl = LineDataSet(entries, " ")

        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.lineWidth = 3f
        vl.fillColor = R.color.purple_500
        vl.fillAlpha = R.color.black

        lineChart.legend.isEnabled = false
        lineChart.xAxis.labelRotationAngle = 0f

        lineChart.data = LineData(vl)

        lineChart.xAxis.axisMaximum += 0.9f
        lineChart.xAxis.axisMinimum -= 0.9f

        lineChart.xAxis.axisLineWidth += 1f
        lineChart.xAxis.axisLineColor = R.color.purple_200


        lineChart.axisRight.isEnabled = false

        lineChart.axisLeft.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(false)

        lineChart.description.text = " "
        lineChart.setNoDataText("LOADING...")

        lineChart.animateX(1800, Easing.EaseInExpo)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEntries(items: ArrayList<String>)
    {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd")
        val formatted = current.format(formatter).toFloat() - 6

        for(i in 0 until (items.size))
        {
            entries.add(Entry((formatted+i), items[i].toFloat()))
        }
    }
}