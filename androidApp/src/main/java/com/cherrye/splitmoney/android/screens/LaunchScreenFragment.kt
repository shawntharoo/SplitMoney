package com.cherrye.splitmoney.android.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.cherrye.splitmoney.android.R
import com.cherrye.splitmoney.android.base.BaseFragmentWithBindings
import com.cherrye.splitmoney.android.databinding.FragmentLaunchScreenBinding
import com.cherrye.splitmoney.viewmodels.LaunchScreenViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import org.koin.android.ext.android.inject

class LaunchScreenFragment : BaseFragmentWithBindings<FragmentLaunchScreenBinding, LaunchScreenViewModel>() {
    override val viewModelClass: Class<LaunchScreenViewModel>
        get() = LaunchScreenViewModel::class.java

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLaunchScreenBinding {
        return FragmentLaunchScreenBinding.inflate(inflater, container, false)
    }

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory { inject<LaunchScreenViewModel>().value }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<Button>(R.id.myButton)?.setOnClickListener {
            viewModel.navigateToNextScreen()
        }
    }
}