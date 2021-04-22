package ru.smak.gpsservice09_962

import android.location.Location
import androidx.lifecycle.MutableLiveData

object LocationData {
    val location: MutableLiveData<Location> = MutableLiveData()
}