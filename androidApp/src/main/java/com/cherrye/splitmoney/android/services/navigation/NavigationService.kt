package com.cherrye.splitmoney.android.services.navigation

import com.cherrye.splitmoney.android.R
import com.cherrye.splitmoney.android.services.presentation.AppContextInitializer
import com.cherrye.splitmoney.constants.routing.RoutingNames
import com.cherrye.splitmoney.services.navigation.NavigationCoreServices

class NavigationService: NavigationCoreServices {
    override fun navigateToViewModel(destination: RoutingNames, parameters: Any?) {
        AppContextInitializer.getCurrentActivity().runOnUiThread {
            when (destination) {
                RoutingNames.launchScreenRoute -> {
                    AppContextInitializer.navHostContainer.navigate(
                        R.id.launchScreenFragment
                    )
                }
                RoutingNames.homeScreenRoute -> {
                    AppContextInitializer.navHostContainer.navigate(
                        R.id.homeScreenFragment
                    )
                }
            }
        }
    }
}