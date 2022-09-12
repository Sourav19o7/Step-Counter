@file:Suppress("DEPRECATION")

package com.level.stepcounter

import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataSourcesResult
import java.util.concurrent.TimeUnit

class SensorySubscribe {
    fun addDataSource(mApiClient:GoogleApiClient, listener: OnDataPointListener)
    {
        Log.i("Connection", "onConnected")
        val dataSourceRequest = DataSourcesRequest.Builder()
            .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build()


        val dataSourcesResultCallback: ResultCallback<DataSourcesResult> =
            ResultCallback<DataSourcesResult> {
                for (dataSource in it.dataSources) {
                    if (DataType.TYPE_STEP_COUNT_CUMULATIVE == dataSource.dataType) {
                        registerFitnessDataListener(
                            mApiClient,
                            listener,
                            dataSource,
                            DataType.TYPE_STEP_COUNT_CUMULATIVE
                        )
                    }
                }
            }

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
            .setResultCallback(dataSourcesResultCallback)
    }

    private fun registerFitnessDataListener(mApiClient: GoogleApiClient, listener: OnDataPointListener, dataSource: DataSource?, dataType: DataType) {
        val request: SensorRequest = SensorRequest.Builder()
            .setDataSource(dataSource!!)
            .setDataType(dataType)
            .setSamplingRate(1, TimeUnit.SECONDS)
            .build()

        Fitness.SensorsApi.add(mApiClient,request,listener)
            .setResultCallback {
                if (it.isSuccess) {
                    Log.i("Connection", "SensorApi Added")
                }
            }
    }

}