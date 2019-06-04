package com.tunebrains.compass

import android.hardware.GeomagneticField
import android.location.Location
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject


class MainViewModel : ViewModel() {
    private val DEGREES_360 = 360
    val targetLocation = Location("").apply {
        longitude = 0.0
        latitude = 0.0
    }
    val userLocation = Location("").apply {
        longitude = 0.0
        latitude = 0.0
    }

    fun valueUpdated(it: Double) {
        val geomagneticField = GeomagneticField(
            userLocation.latitude.toFloat(),
            userLocation.longitude.toFloat(),
            userLocation.altitude.toFloat(), System.currentTimeMillis()
        )
        val azimuth = it - geomagneticField.declination
        var bearTo = userLocation.bearingTo(targetLocation)
        if (bearTo < 0) bearTo += DEGREES_360

        var rotation = bearTo - azimuth
        if (rotation < 0) rotation += DEGREES_360
        degreeObserver.onNext(rotation.toFloat())
    }

    val degreeObserver = PublishSubject.create<Float>()
}