package com.example.coffetable // Make sure this matches your package name

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.annotation.NonNull
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.ExperimentalGetImage
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectAnalyzer(private val tflite: Interpreter, private val labels: List<String>) : ImageAnalysis.Analyzer {
    private val MODEL_INPUT_SIZE = 224 // Set according to your model's input size
    private val OUTPUT_SIZE = 10 // Set this according to your model's output size

    override fun analyze(@NonNull image: ImageProxy) {
        try {
            val bitmap = imageToBitmap(image)
            if (bitmap == null) {
                Log.e("ObjectAnalyzer", "Image conversion to bitmap failed")
                return
            }

            // Preprocess the bitmap for TensorFlow Lite model
            val inputBuffer = preprocessBitmap(bitmap)

            // Prepare output buffer
            val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }

            // Run inference
            tflite.run(inputBuffer, outputBuffer)

            // Process results
            processResults(outputBuffer)
        } catch (e: Exception) {
            Log.e("ObjectAnalyzer", "Error during analysis", e)
        } finally {
            // Always close the ImageProxy when done
            image.close()
        }
    }

    @ExperimentalGetImage
    private fun imageToBitmap(image: ImageProxy): Bitmap? {
        val img: Image? = image.image
        if (img == null) return null

        // Create a bitmap
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)

        // Convert YUV_420_888 to RGB Bitmap
        YUV420888ToBitmap(img, bitmap)

        // Rotate the bitmap to the correct orientation
        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun YUV420888ToBitmap(image: Image, bitmap: Bitmap) {
        // YUV to RGB conversion logic here
        // Implement based on your requirements or use a library to handle the conversion.
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        // Resize the bitmap according to the model's input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)

        val inputBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        resizedBitmap.getPixels(intValues, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE)

        for (pixel in intValues) {
            // Normalize pixel colors
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        return inputBuffer
    }

    private fun processResults(outputBuffer: Array<FloatArray>) {
        // Process the results from the model
        for (i in outputBuffer[0].indices) {
            Log.d("ObjectAnalyzer", "Detected: ${labels[i]} Confidence: ${outputBuffer[0][i]}")
        }
    }
}