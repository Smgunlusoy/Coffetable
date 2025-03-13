package com.example.Coffetable;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ExperimentalGetImage;
import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Analyzes images using a TensorFlow Lite model to perform object detection.
 */
public class ObjectAnalyzer implements ImageAnalysis.Analyzer {
    private static final int MODEL_INPUT_SIZE = 224; // Change according to YOLO model
    private static final int OutputSize = 10; // Set this according to your model's output

    private final Interpreter tflite;
    private final List<String> labels;

    /**
     * Constructs an ObjectAnalyzer.
     *
     * @param tflite  The TensorFlow Lite interpreter to run the model.
     * @param labels  The labels corresponding to the classes in the model.
     */
    public ObjectAnalyzer(Interpreter tflite, List<String> labels) {
        this.tflite = tflite;
        this.labels = labels;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        try {
            // Convert ImageProxy to Bitmap
            @SuppressLint("UnsafeOptInUsageError") Bitmap bitmap = imageToBitmap(image);
            if (bitmap == null) {
                Log.e("ObjectAnalyzer", "Image conversion to bitmap failed");
                return;
            }

            // Preprocess Bitmap for YOLO model
            ByteBuffer inputBuffer = preprocessBitmap(bitmap);

            // Run inference using TFLite
            float[][] outputBuffer = new float[1][OutputSize];
            tflite.run(inputBuffer, outputBuffer);

            // Process results
            processResults(outputBuffer);
        } catch (Exception e) {
            Log.e("ObjectAnalyzer", "Error during analysis", e);
        } finally {
            // Close the image
            image.close();
        }
    }

    /**
     * Converts an ImageProxy to a Bitmap.
     *
     * <p>This method retrieves the image from the ImageProxy and converts it from the YUV_420_888 format to
     * an RGB Bitmap format. Due to changes in the CameraX library, it uses experimental API, hence
     * the caller must opt-in to use this method.</p>
     *
     * @param image The ImageProxy containing the image data to be converted.
     * @return A Bitmap representation of the image, or null if conversion fails.
     */
    @ExperimentalGetImage // Indicates the usage of an experimental API
    private Bitmap imageToBitmap(ImageProxy image) {
        Image img = image.getImage();
        if (img == null) return null;

        // Convert YUV_420_888 to RGB Bitmap
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        YUV420888ToBitmap(img, bitmap); // You should implement this method

        // Rotate bitmap according to camera orientation
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Converts YUV_420_888 format image to RGB Bitmap.
     *
     * @param image The Image object representing the captured image.
     * @param bitmap The bitmap to which the image should be converted.
     */
    private void YUV420888ToBitmap(Image image, Bitmap bitmap) {
        // Implement conversion of YUV to RGB.
        // Refer to CameraX documentation or use available libraries for the conversion.
    }

    private ByteBuffer preprocessBitmap(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE);

        for (int pixelValue : intValues) {
            float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
            float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
            float b = (pixelValue & 0xFF) / 255.0f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    private void processResults(float[][] outputBuffer) {
        for (int i = 0; i < OutputSize; i++) {
            Log.d("ObjectAnalyzer", "Detected: " + labels.get(i) + " Confidence: " + outputBuffer[0][i]);
        }
    }
}