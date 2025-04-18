package com.cherrye.splitmoney.koin

import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual class KoinRuntimeContainer {
    actual companion object : KoinComponent {
        actual fun registerRuntimeService(defaultGen: Module, platformModule: Module
        ) {
            startKoin {
                modules(

                )
            }
        }

        actual fun registerSwiftNativeServices (
            defaultGen: Module,
            navigationService: NativeInjectionFactory<NavigationCoreServices>
        ) {
            startKoin {
                      modules(
                          defaultGen,
                          module {
                              single { navigationService() }
                          }
                      )
            }
        }
    }
}