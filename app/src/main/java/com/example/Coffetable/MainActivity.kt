package com.example.coffetable

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coffetable.ui.CoffeeTableScreen
import com.example.coffetable.ui.theme.CoffetableTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val REQUEST_CAMERA_PERMISSION = 101
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Text-to-Speech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        setContent {
            CoffetableTheme {
                Surface {
                    CoffeeTableScreen(this@MainActivity)
                }
            }
        }

        // Request camera permission on app startup
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera() // Start the camera if permission is already granted
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
                if (previewView != null) { // Ensure the PreviewView is found
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    // Unbind any previously bound use cases
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                    Log.d("CameraX", "Camera started successfully.")
                } else {
                    Log.e("CameraX", "PreviewView is null!")
                }

            } catch (e: Exception) {
                Log.e("CameraX", "Error starting camera: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera() // Start the camera if permission is granted
            } else {
                Log.e("Camera Permission", "Permission denied")
            }
        }
    }

    fun openInternalCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Log.e("Camera", "No app available to handle camera intent")
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    fun openExternalCamera() {
        // Implement your external camera functionality if needed
        Log.d("ExternalCamera", "External camera action not implemented yet.")
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}