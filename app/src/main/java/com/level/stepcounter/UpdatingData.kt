@file:Suppress("DEPRECATION")

package com.level.stepcounter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

class UpdatingData(context: Context) {

    var _context: Context
    lateinit var listener : UpdatingDataListener
    var fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .build()

    init {
        _context = context
    }
    fun updateUI() {
        try {
            listener = _context as UpdatingDataListener
        }catch (e: ClassCastException){
            throw ClassCastException(_context.toString() +
                    "must implement StepDialogListener")
        }
        var calories = ""
        var distance = ""
        var time = ""
        var totalSteps = 0
        Fitness.getHistoryClient(
            _context as Activity,
            GoogleSignIn.getAccountForExtension(_context, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                totalSteps =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt()
                        ?: 0
                listener.updateSteps(totalSteps)
            }
            .addOnFailureListener { e ->
                listener.updateSteps(totalSteps)
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(_context, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

        Fitness.getHistoryClient(
            _context as Activity,
            GoogleSignIn.getAccountForExtension(_context, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener { result ->
                time =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_DURATION)?.toString()
                        ?: "0"
                listener.updateTime(time)
            }
            .addOnFailureListener { e ->
                listener.updateTime("0 min")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(_context, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

        Fitness.getHistoryClient(
            _context as Activity,
            GoogleSignIn.getAccountForExtension(_context, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { result ->
                distance =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_DISTANCE)?.toString()
                        ?: "0"
                listener.updateDis(distance)
            }
            .addOnFailureListener { e ->
                listener.updateDis("0 m")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(_context, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }
        Fitness.getHistoryClient(
            _context as Activity,
            GoogleSignIn.getAccountForExtension(_context, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { result ->
                calories =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_CALORIES)?.toString()
                        ?: "0"
                listener.updateCAl(calories)
            }
            .addOnFailureListener { e ->
                listener.updateCAl("0 kcal")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(_context, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

    }
    interface UpdatingDataListener{
        fun updateTime(time: String)
        fun updateDis(distance: String)
        fun updateCAl(calories: String)
        fun updateSteps(steps: Int)

    }
}