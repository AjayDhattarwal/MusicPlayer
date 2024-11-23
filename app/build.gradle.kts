plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
//    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
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
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("customDebugType") {
            isDebuggable = true
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
        android.buildFeatures.buildConfig = true
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
    implementation(libs.navigation.compose)
    implementation(libs.androidx.material)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation (libs.accompanist.adaptive)

//    implementation(libs.accompanist.systemuicontroller)      //deprecated
    implementation(libs.androidx.core.splashscreen)

//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)



    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    implementation(libs.coil.compose)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    implementation(libs.gson)

    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)


    // Dagger hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    // For instrumentation tests
//    androidTestImplementation(libs.hilt.android.testing)
//    kspAndroidTest(libs.hilt.compiler)

    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Extended Icons
    implementation(libs.androidx.material.icons.extended)


    // Navigation
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.hilt.navigation.compose)

    //image color
    implementation(libs.androidx.palette.ktx)

    //music player
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    implementation(libs.androidx.media)


    //room database
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //ffmpeg
   implementation(libs.ffmpeg.kit.min)


    // mp3 create
    implementation(libs.mpatric.mp3agic)

    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

    implementation ("com.jakewharton.timber:timber:5.0.1")


    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

    implementation("org.jsoup:jsoup:1.15.3")

    //
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging)
    implementation (libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)




}





