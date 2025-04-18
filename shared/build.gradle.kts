
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.multiplatform.resources)

    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit") version "2.1.0"
}

val ktorVersion = "2.1.0"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java).all {
        binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class.java).all {
            export("dev.icerock.moko:mvvm-core:0.16.1")
            export("dev.icerock.moko:mvvm-livedata:0.16.1")
            export("dev.icerock.moko:mvvm-livedata-resources:0.16.1")
            export("dev.icerock.moko:mvvm-state:0.16.1")
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()



    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain {
            dependsOn(commonMain.get())
        }

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            commonMain.dependencies {
                //put your multiplatform dependencies here
                implementation(libs.bundles.ktor)
                api("dev.icerock.moko:mvvm-core:0.16.1") // only ViewModel, EventsDispatcher, Dispatchers.UI
                api("dev.icerock.moko:mvvm-flow:0.16.1") // api mvvm-core, CFlow for native and binding extensions
                api("dev.icerock.moko:mvvm-livedata:0.16.1") // api mvvm-core, LiveData and extensions
                api("dev.icerock.moko:mvvm-state:0.16.1") // api mvvm-livedata, ResourceState class and extensions
                api("dev.icerock.moko:mvvm-livedata-resources:0.16.1") // api mvvm-core, moko-resources, extensions for LiveData with moko-resources
                api("dev.icerock.moko:mvvm-flow-resources:0.16.1")
                api("dev.icerock.moko:kswift-runtime:0.7.0")
                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("io.insert-koin:koin-annotations:1.3.0")
                implementation("co.touchlab:kermit:2.0.4")
                implementation("androidx.datastore:datastore-preferences-core:1.1.1")
                implementation("dev.icerock.moko:resources:0.23.0")
                implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorVersion")
            }
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.android.material)
        }

        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosX64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            iosX64Main.dependsOn(this)

            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
    task("testClasses")
}

android {
    namespace = "com.cherrye.splitmoney"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.matching { it.name == "kspKotlinIosArm64" }.configureEach {
    dependsOn(tasks.getByName("generateMRiosArm64Main"))
}

tasks.matching { it.name == "kspKotlinIosSimulatorArm64" }.configureEach {
    dependsOn(tasks.getByName("generateMRiosSimulatorArm64Main"))
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    with("io.insert-koin:koin-ksp-compiler:1.3.1") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
//    add("kspCommonMainMetadata", "io.insert-koin:koin-ksp-compiler:2.0.0")
//    add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorVersion")
    with("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorVersion") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
}


ksp {
    arg("moduleName", project.name)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.cherrye.splitmoney"
    disableStaticFrameworkWarning = true
}