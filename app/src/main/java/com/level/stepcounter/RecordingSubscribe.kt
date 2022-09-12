@file:Suppress("DEPRECATION")

package com.level.stepcounter

import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessStatusCodes
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.result.ListSubscriptionsResult


class RecordingSubscribe {
    private var mSubscribeResultCallback: ResultCallback<Status>? = null
    private var mCancelSubscriptionResultCallback: ResultCallback<Status>? = null
    private var mListSubscriptionsResultCallback: ResultCallback<ListSubscriptionsResult>? = null

    fun start(mApiClient: GoogleApiClient)
    {
        initCallbacks()
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setResultCallback(mSubscribeResultCallback!!)
    }
    private fun initCallbacks() {
        mSubscribeResultCallback =
            ResultCallback { status ->
                if (status.isSuccess) {
                    if (status.statusCode == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                        Log.i("RecordingAPI", "Already subscribed to the Recording API")
                    } else {
                        Log.i("RecordingAPI", "Not Subscribed to the Recording API")
                    }
                }
            }
        mCancelSubscriptionResultCallback =
            ResultCallback { status ->
                if (status.isSuccess) {
                    Log.i("RecordingAPI", "Canceled subscriptions!")
                } else {
                    Log.i("RecordingAPI", "Failed to cancel subscriptions")
                }
            }
        mListSubscriptionsResultCallback =
            ResultCallback { listSubscriptionsResult ->
                for (subscription in listSubscriptionsResult.subscriptions) {
                    val dataType: DataType? = subscription.dataType
                    Log.i("RecordingAPI", dataType!!.name)
                    for (field in dataType.fields) {
                        Log.i("RecordingAPI", field.toString())
                    }
                }
            }
    }
}