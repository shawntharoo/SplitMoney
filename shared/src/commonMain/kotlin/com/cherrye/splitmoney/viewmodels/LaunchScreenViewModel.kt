package com.cherrye.splitmoney.viewmodels

import com.cherrye.splitmoney.constants.routing.RoutingNames
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import org.koin.core.annotation.Factory

@Factory
class LaunchScreenViewModel : BaseViewModel() {
    private val _counter: MutableLiveData<Int> = MutableLiveData(0)
    val counter: String = _counter.value.toString()
    fun onCounterButtonPressed() {
        val current = _counter.value
        _counter.value = current + 1
    }

    fun navigateToNextScreen() {
        navigationService.value.navigateToViewModel(
            RoutingNames.homeScreenRoute,
            null
        )
    }
}