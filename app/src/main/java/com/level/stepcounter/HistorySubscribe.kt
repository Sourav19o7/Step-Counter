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
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResult
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HistorySubscribe {

    private var WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7
    private val mAdapter: History_Adapter = History_Adapter()


    var info = mutableMapOf<String,String>()
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
            .aggregate(
                DataType.TYPE_DISTANCE_DELTA,
                DataType.AGGREGATE_DISTANCE_DELTA
            )
            .aggregate(DataType.TYPE_MOVE_MINUTES)
            .aggregate(
                DataType.TYPE_CALORIES_EXPENDED,
                DataType.AGGREGATE_CALORIES_EXPENDED
            )
            .aggregate(
                DataType.TYPE_SPEED,
                DataType.AGGREGATE_SPEED_SUMMARY
            )
            .aggregate(DataType.TYPE_HEART_POINTS, DataType.AGGREGATE_HEART_POINTS)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTIme, TimeUnit.MILLISECONDS)
            .build()

        val pendingResult: PendingResult<DataReadResult> =
            Fitness.HistoryApi.readData(mApiClient, readReq)

        pendingResult.setResultCallback {
            if (it.buckets.size > 0) {
                var i = 0
                for (bucket in it.buckets) {
                    i += 1
                    val dataSets: List<DataSet> = bucket.dataSets
                    for (dataSet in dataSets) {
                        processData(dataSet, items, lineChart)
                    }
                }
                if (i == it.buckets.size) {
                    while (items.size < 7) {
                        items.add(0, "0")
                    }
                    Log.i("Sizecc", items.size.toString() + "\n" + info)
                    var lc = LineChart()
                    lc.createchart(lineChart, items)
                    items.clear()
                    CommonAdapter.mAdapter.updateSteps(info)
                }
            }
        }
    }

    private fun processData(dataSet: DataSet, items: ArrayList<String>, lineChart: LineChart) {
        for (dp in dataSet.dataPoints) {
            val dpStart = dp.getStartTime(TimeUnit.NANOSECONDS) / 1000000 +100
            val dpEnd = dp.getEndTime(TimeUnit.NANOSECONDS) / 1000000

            val div = 60 * 60 * 1000;
            val tTime = ((dpEnd - dpStart) + 100000) / div
            val simpleDateFormat = SimpleDateFormat("EEEE")
            val day: String = simpleDateFormat.format(dpStart)
            for (field in dp.dataType.fields) {
                if (field == Field.FIELD_STEPS) {
                    items.add(dp.getValue(field).toString())
                }

                info[day] += ("\n" + field.name + " = "+ dp.getValue(field))
                Log.i(
                    "History",
                    simpleDateFormat.format(dpStart) + " " + field.name + " = " + dp.getValue(field) + " " + tTime
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