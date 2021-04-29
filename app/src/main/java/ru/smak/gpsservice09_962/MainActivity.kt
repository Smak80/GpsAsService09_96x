package ru.smak.gpsservice09_962

import android.Manifest
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var startStopBtn: FloatingActionButton
    private lateinit var output: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startStopBtn = findViewById<FloatingActionButton>(R.id.startStopBtn).apply{
            setOnClickListener {
                if (
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                } else {
                    changeServiceState()
                }
            }
        }
        output = findViewById(R.id.output)
    }

    private fun requestLocationPermission() {
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0
            && permissions.first() == Manifest.permission.ACCESS_FINE_LOCATION
            && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            changeServiceState(true)
        }
    }

    private fun sendCommand(command: String){
        val intent = Intent(this, LocationService::class.java).apply {
            this.action = command
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private val locationObserver: (Location) -> Unit = ::locationChanged

    private fun locationChanged(l: Location){
        val tv = TextView(this@MainActivity)
        tv.textSize = 18F
        tv.text = getString(R.string.position, l.latitude, l.longitude)
        output.addView(tv)
    }

    private fun changeServiceState(forceStart: Boolean = false) {
        if (!LocationService.running || forceStart) {
            sendCommand(Constants.START_LOCATION_SERVICE)
            LocationData.location.observe(this, locationObserver)
        } else {
            sendCommand(Constants.STOP_LOCATION_SERVICE)
            LocationData.location.removeObservers(this)
        }
    }

    override fun onStop() {
        super.onStop()
        LocationData.location.removeObserver(locationObserver)
    }

    override fun onStart() {
        super.onStart()
        if (LocationService.running)
            LocationData.location.observe(this, locationObserver)
    }

}