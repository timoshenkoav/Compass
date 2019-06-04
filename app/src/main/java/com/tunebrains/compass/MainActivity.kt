package com.tunebrains.compass

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var vm: MainViewModel
    private var compositeDisposable = CompositeDisposable()
    private lateinit var sensorManager: CompassSensorManager
    val rxPermissions = RxPermissions(this)
    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm = ViewModelProviders.of(this)[MainViewModel::class.java]
        sensorManager = CompassSensorManager(this.applicationContext)
        locationManager = LocationManager(this.applicationContext)
        lifecycle.addObserver(sensorManager)
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
                Log.d("MainActivity", "New Data: $it")
                vm.valueUpdated(it)
            })
        compositeDisposable.add(sensorManager.azimuthObserver.subscribe({


        }, {

        }))
        compositeDisposable.add(vm.degreeObserver.observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.d("MainActivity", "New degree: $it")
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
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
