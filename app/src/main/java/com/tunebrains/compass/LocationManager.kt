package com.tunebrains.compass

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


class LocationManager(ctx: Context) {
    val locationEvents = BehaviorSubject.create<Location>()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

    companion object {
        private const val TWO_MINUTES: Long = 1000 * 60 * 2
    }

    fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES

        when {
            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            isSignificantlyNewer -> return true
            // If the new location is more than two minutes older, it must be worse
            isSignificantlyOlder -> return false
        }

        // Check whether the new location fix is more or less accurate
        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        // Check if the old and new location are from the same provider
        val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

        // Determine location quality using a combination of timeliness and accuracy
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }

    val clb = object : LocationCallback() {
        var bestKnown: Location? = null
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult ?: return
            var best = locationResult.lastLocation
            if (best != null) {
                for (location in locationResult.locations) {
                    if (isBetterLocation(location, best)) {
                        best = location
                    }
                }
                if (isBetterLocation(best, bestKnown)) {
                    bestKnown = best
                    locationEvents.onNext(bestKnown!!)
                }
            }


        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {

        val request = LocationRequest.create().apply {
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            interval = TimeUnit.SECONDS.toMillis(10)
        }

        fusedLocationClient.requestLocationUpdates(request, clb, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    fun stop() {

        fusedLocationClient.removeLocationUpdates(clb)
    }
}