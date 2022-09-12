@file:Suppress("DEPRECATION")

package com.level.stepcounter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.charts.LineChart
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResult
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HistorySubscribe {

    private var WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7

    var now: Date = Date()
    var endTIme = now.time
    var startTime = endTIme - (WEEK_IN_MS)


    @RequiresApi(Build.VERSION_CODES.O)
    fun retrievingData(
        mApiClient: GoogleApiClient,
        items: ArrayList<String>,
        lineChart: LineChart
    ) {
        setTime()
        val readReq: DataReadRequest = DataReadRequest.Builder()
            .aggregate(
                DataType.TYPE_STEP_COUNT_DELTA,
                DataType.AGGREGATE_STEP_COUNT_DELTA
            )
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTIme, TimeUnit.MILLISECONDS)
            .build()

        val pendingResult: PendingResult<DataReadResult> =
            Fitness.HistoryApi.readData(mApiClient, readReq)

        pendingResult.setResultCallback {
            if (it.buckets.size > 0) {
                Log.i("History", it.buckets.size.toString())
                for (bucket in it.buckets) {
                    val dataSets: List<DataSet> = bucket.dataSets
                    for (dataSet in dataSets) {
                        processData(dataSet, items, lineChart)
                        var lc = LineChart()
                        if (items.size == 7) {
                            lc.createchart(lineChart, items)
                        }

                    }
                }

            }
        }
    }

    private fun processData(dataSet: DataSet, items: ArrayList<String>, lineChart: LineChart) {
        for (dp in dataSet.dataPoints) {
            val dpStart = dp.getStartTime(TimeUnit.NANOSECONDS) / 1000000 + 100000
            val dpEnd = dp.getEndTime(TimeUnit.NANOSECONDS) / 1000000

            val div = 60 * 60 * 1000;
            val tTime = ((dpEnd - dpStart) + 100000) / div
            val simpleDateFormat = SimpleDateFormat("EEEE")

            for (field in dp.dataType.fields) {
                items.add(dp.getValue(field).toString())
                Log.i(
                    "History",
                    simpleDateFormat.format(dpStart) + " " + dp.getValue(field) + " " + tTime
                )
            }
        }
    }

    private fun setTime() {
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Long = (rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET)).toLong()

        val sinceMidnight: Long = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000)

        startTime += (24 * 60 * 60 * 1000 - sinceMidnight)
    }
}