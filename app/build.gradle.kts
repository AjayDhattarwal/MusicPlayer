plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.ar.musicplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ar.musicplayer"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.navigation.compose)

    implementation (libs.androidx.material)

    implementation (libs.androidx.runtime.livedata)
    implementation (libs.androidx.lifecycle.viewmodel.compose)


    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.gson)

    implementation (libs.logging.interceptor)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)


    // Dagger hilt
    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)
    testImplementation (libs.hilt.android.testing)
    // For instrumentation tests
    androidTestImplementation  (libs.hilt.android.testing)
    kspAndroidTest (libs.hilt.compiler)

    ksp ("androidx.hilt:hilt-compiler:1.2.0")

    // Extended Icons
    implementation (libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.hilt.navigation.compose)

    //image color
    implementation (libs.androidx.palette.ktx)

    //music player
    implementation (libs.accompanist.systemuicontroller)
    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.exoplayer.core)
    // ExoPlayer UI module (contains PlayerNotificationManager)
    implementation (libs.exoplayer.ui)
    implementation (libs.androidx.media)


    implementation ("com.github.bumptech.glide:compose:1.0.0-beta01")

    implementation ("com.google.accompanist:accompanist-glide:0.10.0")

    //room database
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //ffmpeg
    implementation (libs.ffmpeg.kit.full)

    // mp3 create
    implementation (libs.mpatric.mp3agic)

//    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")
//    implementation("androidx.tracing:tracing-perfetto:1.0.0")
//    implementation("androidx.tracing:tracing-perfetto-binary:1.0.0")


}
