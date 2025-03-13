plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Use the Kotlin DSL for Android
    id("kotlin-kapt") // If you are using KAPT for annotations
}

android {
    compileSdk = 34 // Update this to at least 33

    namespace = "com.example.coffetable"

    defaultConfig {
        applicationId = "com.example.coffetable"
        minSdk = 21 // or your minimum SDK version
        targetSdk = 34 // Keep this updated to the latest stable version
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Set this to 1.8
        targetCompatibility = JavaVersion.VERSION_1_8 // Set this to 1.8
    }

    kotlinOptions {
        jvmTarget = "1.8" // Ensure this matches the Java versions
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-view:1.2.0") // Use the latest version available
    implementation("org.tensorflow:tensorflow-lite:2.6.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.6.0") // if you're using GPU
    implementation("com.google.android.material:material:1.9.0") // Keep updated
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.0") // Ensure this matches your Kotlin plugin version

    // Add other dependencies as needed...
}