package com.cherrye.splitmoney.android.screens

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.cherrye.splitmoney.android.base.BaseFragmentWithBindings
import com.cherrye.splitmoney.android.databinding.FragmentHomeScreenBinding
import com.cherrye.splitmoney.viewmodels.HomeScreenViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import org.koin.android.ext.android.inject

class HomeScreenFragment : BaseFragmentWithBindings<FragmentHomeScreenBinding, HomeScreenViewModel>() {
    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeScreenBinding {
        return FragmentHomeScreenBinding.inflate(inflater, container, false)
    }

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory { inject<HomeScreenViewModel>().value }
    }

}