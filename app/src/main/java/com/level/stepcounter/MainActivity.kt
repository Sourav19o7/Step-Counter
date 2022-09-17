@file:Suppress("DEPRECATION")

package com.level.stepcounter

import CustomMarker
import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.OnDataPointListener
import com.level.stepcounter.databinding.ActivityMainBinding
import java.lang.Integer.min


class MainActivity : AppCompatActivity(), OnDataPointListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, StepDialog.StepDialogListener {


    val sessionManager = SessionManager(this@MainActivity)

    var items = ArrayList<String>()
    var totalSteps = 0
    var progressMax = 10000

    lateinit var binding: ActivityMainBinding
    private val FINE_LOCATION = 101
    private val ACTIVITY_RECO = 102


    var str_permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val REQUEST_OAUTH = 1
    val AUTH_PENDING: String = ""
    var authInProgress: Boolean = false

    lateinit var mApiClient: GoogleApiClient
    var fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .build()

    var call = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //request()

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING)
        }

        mApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
            .addScope(Fitness.SCOPE_LOCATION_READ)
            .addScope(Fitness.SCOPE_BODY_READ)
            .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }

    fun request() {
        checkForPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            "location",
            FINE_LOCATION
        )
        checkForPermission(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            "activity",
            ACTIVITY_RECO
        )
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission)
                        == PackageManager.PERMISSION_GRANTED -> {
                            mApiClient.connect()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )
                else -> ActivityCompat.requestPermissions(this, str_permissions, requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                for (permission in permissions) {
                    checkForPermission(permission, name, requestCode)
                }
                Log.i("Connection", "$name permission refused")

            } else {
                Log.i("Connection", "$name permission granted")
                mApiClient.connect()
            }
        }

        when (requestCode) {
            FINE_LOCATION -> innerCheck("location")
            ACTIVITY_RECO -> innerCheck("activity")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to access your $name is required to use the app")
            setTitle("Permission Required")
            setPositiveButton("Allow") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        request()

        totalSteps = sessionManager.getSteps()
        binding.targetSteps.setOnClickListener {
            openDialog()
        }
    }

    private fun openDialog() {
        val stepDialog = StepDialog()
        stepDialog.show(supportFragmentManager, "TARGET")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AUTH_PENDING, authInProgress)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false
            if (resultCode == RESULT_OK) {
                if (!mApiClient.isConnecting && !mApiClient.isConnected) {
                    mApiClient.connect()
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("Connection", "Request Cancelled")
            }
        } else {
            Log.i("Connection", "Not request Oauth")
        }
    }

    override fun onDataPoint(p0: DataPoint) {
        Log.i("Connection", "onDataPoint")
        var t = "0"
        for (field: Field in p0.dataType.fields) {
            p0.getValue(field)
            runOnUiThread {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        var calories = ""
        var distance = ""
        var time = ""
        Fitness.getHistoryClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                totalSteps =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt()
                        ?: 0
                updateSteps()
            }
            .addOnFailureListener { e ->
                updateSteps()
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

        Fitness.getHistoryClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener { result ->
                time =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_DURATION)?.toString()
                        ?: "0"
                updateTime(time)
            }
            .addOnFailureListener { e ->
                updateTime("0 min")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

        Fitness.getHistoryClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { result ->
                distance =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_DISTANCE)?.toString()
                        ?: "0"
                updateDis(distance)
            }
            .addOnFailureListener { e ->
                updateDis("0 m")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }
        Fitness.getHistoryClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { result ->
                calories =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_CALORIES)?.toString()
                        ?: "0"
                updateCAl(calories)
            }
            .addOnFailureListener { e ->
                updateCAl("0 kcal")
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }

    }

    private fun updateTime(time: String) {
        findViewById<TextView>(R.id.time_text).text = time + " mins"
    }

    private fun updateDis(distance: String) {

        findViewById<TextView>(R.id.distance_text).text = distance.substring(0,min(distance.length,6)) + " m"
    }

    private fun updateCAl(calories: String) {
        findViewById<TextView>(R.id.calories_text).text = calories.substring(0,min(calories.length,5)) + " kcal"
    }

    private fun updateSteps() {
        val progress_bar = findViewById<ProgressBar>(R.id.progress_bar)
        val text_view = findViewById<TextView>(R.id.text_view_progress)

        progress_bar.max = progressMax
        progress_bar.progress = totalSteps
        text_view.text = totalSteps.toString()

    }

    override fun onStop() {
        super.onStop()

        Fitness.SensorsApi.remove(mApiClient, this)
            .setResultCallback {

                if (it.isSuccess) {
                    mApiClient.disconnect()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onConnected(p0: Bundle?) {
        val hs = HistorySubscribe()
        hs.retrievingData(mApiClient, items, binding.lineChart)
        val rs = RecordingSubscribe()
        rs.start(mApiClient)
        val ss = SensorySubscribe()
        ss.addDataSource(mApiClient, this@MainActivity)
        val intent = Intent(this, ArchiveActivity::class.java)
        val markerView = CustomMarker(this@MainActivity, R.layout.marker_layout)
        binding.lineChart.marker = markerView
        binding.lineChart.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                startActivity(intent)
            }
        })
    }

    abstract class DoubleClickListener : View.OnClickListener {
        var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v)
            }
            lastClickTime = clickTime
        }

        abstract fun onDoubleClick(v: View?)

        companion object {
            private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true
                p0.startResolutionForResult(this, REQUEST_OAUTH)
            } catch (e: IntentSender.SendIntentException) {
                Log.i("Connection", e.toString())
            }
        } else {
            Log.i("Connection", "authOnProgress")
        }
    }

    override fun applyTarget(target: String) {
        binding.targetSteps.text = target
        sessionManager.updateTarget(totalSteps)
        progressMax = target.substring(1).toInt()
        updateSteps()
    }
}
