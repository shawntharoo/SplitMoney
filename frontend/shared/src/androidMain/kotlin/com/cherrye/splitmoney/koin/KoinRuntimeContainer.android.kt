package com.cherrye.splitmoney.koin

import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import org.koin.core.context.startKoin
import org.koin.core.module.Module

actual class KoinRuntimeContainer {
    actual companion object {
        actual fun registerRuntimeService(
            defaultGen: Module, platformModule: Module
        ) {
            startKoin {
                modules(
                    defaultGen,
                    platformModule
                )
            }
        }

        actual fun registerSwiftNativeServices (
            defaultGen: Module,
            navigationService: NativeInjectionFactory<NavigationCoreServices>
        ) {

        }
    }
}