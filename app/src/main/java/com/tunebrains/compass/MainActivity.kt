package com.tunebrains.compass

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var vm: MainViewModel
    private var compositeDisposable = CompositeDisposable()
    private lateinit var sensorManager: CompassSensorManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm = ViewModelProviders.of(this)[MainViewModel::class.java]
        sensorManager = CompassSensorManager(this)
        lifecycle.addObserver(sensorManager)
    }

    override fun onResume() {
        super.onResume()

        compositeDisposable.add(sensorManager.azimuthObserver.subscribe({
            Log.d("MainActivity", "New azimuth: $it")
            vm.valueUpdated(it)
        }, {

        }))
        compositeDisposable.add(vm.degreeObserver.observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.d("MainActivity", "New degree: $it")
            compass.updateDegree(it)
        }, {

        }))
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
