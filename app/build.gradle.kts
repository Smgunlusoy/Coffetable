plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.coffetable" // Use your actual package name
    compileSdk = 31 // Update this to your desired SDK version

    defaultConfig {
        applicationId = "com.example.coffetable"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.10.0") // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite-support:0.3.1") // TensorFlow Lite Support
    implementation("androidx.camera:camera-core:1.0.0") // CameraX Core
    implementation("androidx.camera:camera-camera2:1.0.0") // CameraX Camera
    implementation("androidx.camera:camera-lifecycle:1.0.0") // CameraX Lifecycle
    implementation("androidx.camera:camera-view:1.0.0") // CameraX View

    // Optional: Add OpenCV if needed
    implementation("org.opencv:opencv-android:4.5.3") // Check for the latest version
}
