package com.cherrye.splitmoney.android.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.cherrye.splitmoney.viewmodels.BaseViewModel
import dev.icerock.moko.mvvm.MvvmFragment

abstract class BaseFragmentWithBindings<TBinding: ViewDataBinding, TViewModel: BaseViewModel> : MvvmFragment<TBinding, TViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
}