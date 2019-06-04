package com.tunebrains.compass

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject


class MainViewModel : ViewModel() {
    val degreeObserver = PublishSubject.create<Double>()
}