package com.cherrye.splitmoney.koin

import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import org.koin.core.module.Module
import org.koin.core.scope.Scope

typealias NativeInjectionFactory<T> = Scope.() -> T
expect class KoinRuntimeContainer {
    companion object {
        fun registerRuntimeService(defaultGen: Module, platformModule: Module
        )

        fun registerSwiftNativeServices(
            defaultGen: Module,
            navigationService: NativeInjectionFactory<NavigationCoreServices>
        )
    }
}