package com.cherrye.splitmoney.viewmodels

import dev.icerock.moko.mvvm.livedata.MutableLiveData

class LaunchScreenViewModel : BaseViewModel() {
    private val _counter: MutableLiveData<Int> = MutableLiveData(0)
    val counter: String = _counter.value.toString()
    fun onCounterButtonPressed() {
        val current = _counter.value
        _counter.value = current + 1
    }
}