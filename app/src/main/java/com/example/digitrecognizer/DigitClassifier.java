package com.example.digitrecognizer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Wraps the TensorFlow Lite model.
 * Responsibilities: load model, preprocess bitmap, run inference, return probabilities.
 */
public class DigitClassifier {

    private static final String MODEL_FILE = "mnist.tflite"; // file inside assets/
    private static final int IMG_SIZE = 28;                  // MNIST images are 28x28
    private static final int NUM_CLASSES = 10;               // digits 0-9

    private final Interpreter interpreter; // runs the model

    /** Loads the model from assets and creates the TFLite Interpreter. */
    public DigitClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
    }

    /** Memory-maps the .tflite file from the assets folder (fast, no extra copy). */
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    /**
     * Runs the model on the drawing.
     * @return array of 10 probabilities (index = digit), or null if the canvas is empty
     */
    public float[] classify(Bitmap source) {
        // 1) Preprocess: shrink the drawing to 28x28 pixels
        Bitmap scaled = Bitmap.createScaledBitmap(source, IMG_SIZE, IMG_SIZE, true);

        // 2) Input buffer: shape [1, 28, 28, 1], float32 -> 4 bytes per value
        ByteBuffer input = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE);
        input.order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMG_SIZE * IMG_SIZE];
        scaled.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

        float sum = 0f;
        for (int pixel : pixels) {
            // Drawing is white on black, so the red channel equals the grayscale value (0-255)
            int gray = (pixel >> 16) & 0xFF;
            float value = gray / 255.0f; // normalize to 0..1 (same as in training)
            sum += value;
            input.putFloat(value);
        }
        if (sum == 0f) return null; // nothing drawn

        // 3) Output buffer: shape [1, 10] (one probability per digit)
        float[][] output = new float[1][NUM_CLASSES];

        // 4) Run inference
        interpreter.run(input, output);

        return output[0];
    }

    /** Releases model resources. */
    public void close() {
        interpreter.close();
    }
}