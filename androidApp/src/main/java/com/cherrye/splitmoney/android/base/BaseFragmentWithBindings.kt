package com.cherrye.splitmoney.android.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.cherrye.splitmoney.viewmodels.BaseViewModel
import dev.icerock.moko.mvvm.viewbinding.MvvmFragment

abstract class BaseFragmentWithBindings<TBinding: ViewBinding, TViewModel: BaseViewModel> : MvvmFragment<TBinding, TViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}