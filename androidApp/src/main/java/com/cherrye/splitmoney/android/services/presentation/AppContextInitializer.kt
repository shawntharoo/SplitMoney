package com.cherrye.splitmoney.android.services.presentation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import org.koin.core.component.KoinComponent

class AppContextInitializer {
    companion object : KoinComponent {
        internal var clientAppContext: FragmentActivity? = null
        internal lateinit var navHostContainer: NavController;
        fun getCurrentActivity() : FragmentActivity {
            return clientAppContext!!
        }

        fun initializeActivityPreServices(context: AppCompatActivity) {
            clientAppContext = context
        }
    }
}