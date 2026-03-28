package com.cherrye.splitmoney.viewmodels

import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class BaseViewModel : ViewModel(), KoinComponent {

    val navigationService = inject<NavigationCoreServices>();
    open fun onInitialize() {

    }
}