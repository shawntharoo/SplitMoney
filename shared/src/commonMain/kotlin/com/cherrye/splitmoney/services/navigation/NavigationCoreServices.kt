package com.cherrye.splitmoney.services.navigation

import com.cherrye.splitmoney.constants.routing.RoutingNames

interface NavigationCoreServices {
    fun navigateToViewModel(destination: RoutingNames, parameters: Any?)
}