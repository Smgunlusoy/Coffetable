package com.example.coffetable

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.ExperimentalGetImage
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Analyzes images using a TensorFlow Lite model to perform object detection.
 */
class ObjectAnalyzer(
    private val tflite: Interpreter,
    private val labels: List<String>
) : ImageAnalysis.Analyzer {

    private val MODEL_INPUT_SIZE = 224 // Adjust according to your model's input size
    private val OUTPUT_SIZE = 10 // Set according to your model's output size

    override fun analyze(image: ImageProxy) {
        try {
            // Convert ImageProxy to Bitmap
            @SuppressLint("UnsafeOptInUsageError")
            val bitmap = imageToBitmap(image)
            if (bitmap == null) {
                Log.e("ObjectAnalyzer", "Image conversion to bitmap failed")
                return
            }

            // Preprocess the Bitmap for the model
            val inputBuffer: ByteBuffer = preprocessBitmap(bitmap)

            // Run inference using TensorFlow Lite
            val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }
            tflite.run(inputBuffer, outputBuffer)

            // Process results
            processResults(outputBuffer)
        } catch (e: Exception) {
            Log.e("ObjectAnalyzer", "Error during analysis", e)
        } finally {
            image.close() // Ensure the ImageProxy is closed
        }
    }

    @ExperimentalGetImage // Usage of an experimental API
    private fun imageToBitmap(image: ImageProxy): Bitmap? {
        val img = image.image ?: return null

        // Convert YUV_420_888 to RGB Bitmap
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        YUV420888ToBitmap(img, bitmap)

        // Rotate bitmap according to camera orientation
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun YUV420888ToBitmap(image: Image, bitmap: Bitmap) {
        // Implement the conversion from YUV to RGB here.
        // This typically involves manipulating the Y, U, and V channels of the image.
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)

        val inputBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        resizedBitmap.getPixels(intValues, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE)

        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        return inputBuffer
    }

    private fun processResults(outputBuffer: Array<FloatArray>) {
        for (i in outputBuffer[0].indices) {
            Log.d("ObjectAnalyzer", "Detected: ${labels[i]} Confidence: ${outputBuffer[0][i]}")
        }
    }
}