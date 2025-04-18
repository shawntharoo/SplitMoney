package com.cherrye.splitmoney.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.cherrye.splitmoney.android.databinding.ActivityCoreBinding
import com.cherrye.splitmoney.android.services.presentation.AppContextInitializer


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextInitializer.initializeActivityPreServices(this)
        val binding = ActivityCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Get the NavController associated with the NavHostFragment
        AppContextInitializer.navHostContainer = navHostFragment.navController
    }
}
