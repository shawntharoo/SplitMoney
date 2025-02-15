package com.cherrye.splitmoney.android

import android.app.Application
import com.cherrye.splitmoney.viewmodels.LaunchScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    viewModel { LaunchScreenViewModel() }
                }
            )  // Your Koin module(s) here
        }
    }
}