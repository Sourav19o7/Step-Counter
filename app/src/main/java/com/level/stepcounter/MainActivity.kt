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


class MainActivity : AppCompatActivity(), OnDataPointListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {


    var items = ArrayList<String>()

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
        request()

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING)
        }

        mApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
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
                Log.i("Connection", "$name permission granted")
            } else {
                Log.i("Connection", "$name permission refused")
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
        mApiClient.connect()
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
        Fitness.getHistoryClient(
            this,
            GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        )
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                val totalSteps =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt()
                        ?: 0
                updateComponents(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.i("Connection", "There was a problem getting steps.", e)
                Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateComponents(totalSteps: Int) {
        val progress_bar = findViewById<ProgressBar>(R.id.progress_bar)
        val text_view = findViewById<TextView>(R.id.text_view_progress)

        progress_bar.max = 10000
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

        val markerView = CustomMarker(this@MainActivity, R.layout.marker_layout)
        binding.lineChart.marker = markerView
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
}
