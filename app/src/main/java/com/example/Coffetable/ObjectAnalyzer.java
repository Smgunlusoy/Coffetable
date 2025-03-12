import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class ObjectAnalyzer implements ImageAnalysis.Analyzer {
    private static final int MODEL_INPUT_SIZE = 224; // YOLO modelin giriş boyutuna göre değiştirin
    private static final int OutputSize = 10; // Modelinizin çıkış boyutuna göre ayarlayın

    private final Interpreter tflite;
    private final List<String> labels;

    public ObjectAnalyzer(Interpreter tflite, List<String> labels) {
        this.tflite = tflite;
        this.labels = labels;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        try {
            // Convert ImageProxy to Bitmap
            Bitmap bitmap = imageToBitmap(image);

            // Preprocess Bitmap for YOLO model
            ByteBuffer inputBuffer = preprocessBitmap(bitmap);

            // Run inference using TFLite
            float[][] outputBuffer = new float[1][OutputSize];
            tflite.run(inputBuffer, outputBuffer);

            // Process results
            processResults(outputBuffer);
        } catch (Exception e) {
            Log.e("ObjectAnalyzer", "Error during analysis: " + e.getMessage());
        } finally {
            // Close the image
            image.close();
        }
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        Image img = image.getImage();
        if (img == null) return null;

        // Convert YUV_420_888 to RGB Bitmap
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        image.close();

        // Döndürme işlemi: Kamera açısına göre ayarla
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
