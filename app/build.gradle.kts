plugins {
    id("com.android.application") version "8.0.2"
    id("org.jetbrains.kotlin.android") version "1.8.21"
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    compileSdk = 34

    namespace = "com.example.coffetable"

    defaultConfig {
        applicationId = "com.example.coffetable"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        kapt {
            arguments {
                // Specify any arguments needed by the annotation processor
                arg("key", "value") // Example argument
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

    // Compose options
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }
}

dependencies {
    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.4.0") // Update to the latest version

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.11.0") // En güncel sürümü kullanın
    implementation("org.tensorflow:tensorflow-lite-gpu:2.11.0") // En güncel sürümü kullanın
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21") // Kotlin.

    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.compose.ui:ui-tooling:1.4.0") // Debug tools for Compose
}