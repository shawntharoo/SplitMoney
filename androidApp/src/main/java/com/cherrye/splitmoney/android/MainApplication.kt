package com.cherrye.splitmoney.android

import android.app.Application
import com.cherrye.splitmoney.android.services.navigation.NavigationService
import com.cherrye.splitmoney.koin.KoinRuntimeContainer
import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        KoinRuntimeContainer.registerRuntimeService(
            defaultModule,
            module {
                singleOf(::NavigationService) { bind<NavigationCoreServices>() }
            }
        )
    }
}