package com.tunebrains.compass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    lateinit var vm: MainViewModel
    var compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm = ViewModelProviders.of(this)[MainViewModel::class.java]

    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(vm.degreeObserver.observeOn(AndroidSchedulers.mainThread()).subscribe({

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
