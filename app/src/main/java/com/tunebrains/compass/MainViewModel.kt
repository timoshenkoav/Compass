package com.tunebrains.compass

import android.hardware.GeomagneticField
import android.location.Location
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

data class UserData(val azimuth: Double, val location: Location)
class MainViewModel : ViewModel() {
    private val DEGREES_360 = 360
    val targetLocation = Location("").apply {
        longitude = 0.0
        latitude = 0.0
    }


    fun valueUpdated(it: UserData) {
        val geomagneticField = GeomagneticField(
            it.location.latitude.toFloat(),
            it.location.longitude.toFloat(),
            it.location.altitude.toFloat(), System.currentTimeMillis()
        )
        val azimuth = it.azimuth - geomagneticField.declination
        var bearTo = it.location.bearingTo(targetLocation)
        if (bearTo < 0) bearTo += DEGREES_360

        var rotation = bearTo - azimuth
        if (rotation < 0) rotation += DEGREES_360
        degreeObserver.onNext(rotation.toFloat())
    }

    val degreeObserver = PublishSubject.create<Float>()
}