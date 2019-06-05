package com.tunebrains.compass

import android.hardware.GeomagneticField
import android.location.Location
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

data class UserData(val azimuth: Double, val location: Location)
class MainViewModel : ViewModel() {
    val degreeObserver = PublishSubject.create<Float>()

    companion object {
        private const val DEGREES_360 = 360
        private val NORTH_POLE = Location("").apply {
            longitude = 0.0
            latitude = 90.0
        }
    }

    private var oldValue = 0f
    private val targetLocation = NORTH_POLE

    private fun lowPassFilter(rawValue: Float): Float {
        val alpha = 0.95f

        return alpha * oldValue + (1.0f - alpha) * rawValue
    }

    fun updateTarget(latitude: Double, longitude: Double) {
        synchronized(this) {
            targetLocation.longitude = longitude
            targetLocation.latitude = latitude
        }
    }

    fun valueUpdated(it: UserData) {
        val geomagneticField = GeomagneticField(
            it.location.latitude.toFloat(),
            it.location.longitude.toFloat(),
            it.location.altitude.toFloat(), System.currentTimeMillis()
        )
        val azimuth = it.azimuth - geomagneticField.declination

        var bearTo = synchronized(targetLocation) {
            it.location.bearingTo(targetLocation)
        }
        if (bearTo < 0) bearTo += DEGREES_360

        var rotation = bearTo - azimuth

        if (rotation < 0) rotation += DEGREES_360
        oldValue = lowPassFilter(rotation.toFloat())
        degreeObserver.onNext(oldValue)

    }


}