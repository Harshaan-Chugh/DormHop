plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    // hilt
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.dormhopfrontend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dormhopfrontend"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    //google sign in
    implementation(libs.play.services.auth)
    implementation("com.google.android.gms:play-services-identity:18.1.0")
    implementation(libs.play.services.auth.v2060)

    // Credential Manager core
    implementation(libs.androidx.credentials.v122)
    // support for Google Play–backed credentials on older Android
    implementation(libs.androidx.credentials.play.services.auth.v122)
    // Google ID helper library (for Google ID tokens)
    implementation(libs.googleid.v100)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // okhttp3
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // gson
    implementation(libs.converter.gson)
    // coil
    implementation("io.coil-kt:coil-compose:2.4.0")


    implementation("androidx.navigation:navigation-compose:2.7.0")
    // Jetpack Compose Material 3
    implementation("androidx.compose.material3:material3:1.1.0")
    // Material Icons (the “FilterList” icon lives here)
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}