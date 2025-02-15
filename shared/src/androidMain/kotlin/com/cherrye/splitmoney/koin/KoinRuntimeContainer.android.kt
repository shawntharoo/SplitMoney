package com.cherrye.splitmoney.koin

import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

actual class KoinRuntimeContainer {
    actual companion object {
        actual fun registerRuntimeService(
        ) {
            startKoin {
                androidLogger()
            }
        }
    }
}