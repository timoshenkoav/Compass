package com.tunebrains.compass

import android.Manifest
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel
    private lateinit var sensorManager: CompassSensorManager
    private lateinit var locationManager: LocationManager
    private var compositeDisposable = CompositeDisposable()
    private val rxPermissions = RxPermissions(this)

    private fun isValidLng(lng: Double): Boolean {
        if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }

    private fun isValidLat(lat: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm = ViewModelProviders.of(this)[MainViewModel::class.java]
        sensorManager = CompassSensorManager(this.applicationContext)
        locationManager = LocationManager(this.applicationContext)
        lifecycle.addObserver(sensorManager)
        updateTarget.setOnClickListener {
            val longitude = edLongitude.text.toString().toDoubleOrNull()
            if (longitude == null || !isValidLng(longitude)) {
                edLongitude.error = getString(R.string.invalid_value)
                return@setOnClickListener
            }
            val latitide = edLatitude.text.toString().toDoubleOrNull()
            if (latitide == null || !isValidLat(latitide)) {
                edLatitude.error = getString(R.string.invalid_value)
                return@setOnClickListener
            }
            vm.updateTarget(latitide, longitude)
        }
    }


    override fun onResume() {
        super.onResume()

        compositeDisposable.add(
            rxPermissions.request(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).subscribe {
                if (it) {
                    locationManager.start()
                }
            }
        )
        compositeDisposable.add(
            Observable.combineLatest(
                sensorManager.azimuthObserver,
                locationManager.locationEvents,
                BiFunction<Double, Location, UserData> { azimuth, location ->
                    UserData(azimuth, location)
                }).subscribe {
                vm.valueUpdated(it)
            })

        compositeDisposable.add(vm.degreeObserver.observeOn(AndroidSchedulers.mainThread()).subscribe({
            compass.updateDegree(it)
        }, {

        }))
    }

    override fun onPause() {
        locationManager.stop()
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(sensorManager)
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
