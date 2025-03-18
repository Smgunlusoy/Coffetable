package com.example.coffetable

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import com.example.coffetable.ui.CoffeeTableScreen
import com.example.coffetable.ui.theme.CoffetableTheme
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale
import java.util.ArrayList

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private val REQUEST_CAMERA_PERMISSION = 101
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Text-to-Speech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error occurred: $error")
            }
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    for (result in it) {
                        if (result.equals("open camera", ignoreCase = true)) {
                            openInternalCamera()
                        }
                    }
                }
            }
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        startListening()

        // Request camera permissions
        requestCameraPermission()

        setContent {
            CoffetableTheme {
                Surface {
                    CoffeeTableScreen(this) // Pass the activity reference as needed
                }
            }
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizer.startListening(intent)
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
            labels = loadLabels() // This should correctly return a List<String>
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
        val labelsList = ArrayList<String>() // Use ArrayList for compatibility with Java's List
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

                val previewView = findViewById<PreviewView>(R.id.previewView)
                preview.setSurfaceProvider(previewView.surfaceProvider)
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ObjectAnalyzer(tflite, labels))

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

    fun openExternalCamera() {
        val intent = Intent("com.example.EXTERNAL_CAMERA_ACTION")
        startActivity(intent)
    }

    fun openInternalCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}