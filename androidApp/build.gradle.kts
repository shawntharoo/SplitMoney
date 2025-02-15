plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.cherrye.splitmoney.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.cherrye.splitmoney.android"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigationCompose)
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("dev.icerock.moko:mvvm-livedata-material:0.16.1") // api mvvm-livedata, Material library android extensions
    implementation("dev.icerock.moko:mvvm-livedata-glide:0.16.1") // api mvvm-livedata, Glide library android extensions
    implementation("dev.icerock.moko:mvvm-livedata-swiperefresh:0.16.1") // api mvvm-livedata, SwipeRefreshLayout library android extensions
    implementation("dev.icerock.moko:mvvm-viewbinding:0.16.1")
    implementation("androidx.fragment:fragment:1.8.5") // Required for FragmentContainerView
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.1")
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.android.material)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.android.material)
    debugImplementation(libs.compose.ui.tooling)
}