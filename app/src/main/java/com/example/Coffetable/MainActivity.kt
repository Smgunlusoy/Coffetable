package com.example.coffetable // Change this to match your actual package name

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView // Ensure this import is present
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA_PERMISSION = 101
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this matches your layout file name

        // Request camera permissions
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            loadModel()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun loadModel() {
        try {
            tflite = Interpreter(loadModelFile("yolov5s.tflite"))
            labels = loadLabels()
        } catch (e: Exception) {
            Log.e("YOLO", "Model loading error", e)
        }
    }

    private fun loadModelFile(modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val fileChannel = FileInputStream(fileDescriptor.fileDescriptor).channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(): List<String> {
        val labelsList = mutableListOf<String>()
        try {
            BufferedReader(InputStreamReader(assets.open("coco_labels.txt"))).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    labelsList.add(line!!)
                }
            }
        } catch (e: IOException) {
            Log.e("YOLO", "Error loading labels", e)
        }
        return labelsList
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val preview = Preview.Builder().build()
                val imageAnalysis = ImageAnalysis.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                // Ensure we are using PreviewView
                val previewView = findViewById<PreviewView>(R.id.previewView)

                // Set the surface provider for preview
                preview.setSurfaceProvider(previewView.surfaceProvider)

                // Set the image analyzer
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ObjectAnalyzer(tflite, labels))

                // Bind the camera lifecycle to this activity
                cameraProviderFuture.get().bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("CameraX", "Error starting camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadModel()
                    startCamera()
                } else {
                    Log.e("Camera Permission", "Permission denied")
                }
            }
        }
    }
}