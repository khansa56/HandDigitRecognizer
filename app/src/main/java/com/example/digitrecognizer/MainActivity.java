package com.example.digitrecognizer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private DigitClassifier classifier;
    private TextView tvResult, tvTime, tvHistory;
    private TextView[] labels = new TextView[3]; // top-3 text labels
    private ProgressBar[] bars = new ProgressBar[3]; // top-3 confidence bars
    private final StringBuilder history = new StringBuilder();
    private int historyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Java variables with the XML views
        drawingView = findViewById(R.id.drawingView);
        tvResult = findViewById(R.id.tvResult);
        tvTime = findViewById(R.id.tvTime);
        tvHistory = findViewById(R.id.tvHistory);
        labels[0] = findViewById(R.id.tvLabel1);
        labels[1] = findViewById(R.id.tvLabel2);
        labels[2] = findViewById(R.id.tvLabel3);
        bars[0] = findViewById(R.id.pb1);
        bars[1] = findViewById(R.id.pb2);
        bars[2] = findViewById(R.id.pb3);
        Button btnPredict = findViewById(R.id.btnPredict);
        Button btnClear = findViewById(R.id.btnClear);

        // Load the TFLite model once when the app starts
        try {
            classifier = new DigitClassifier(this);
        } catch (IOException e) {
            Toast.makeText(this, "Model loading failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnPredict.setOnClickListener(v -> predict());
        btnClear.setOnClickListener(v -> clearAll());
    }

    /** Runs the model on the current drawing and shows the results. */
    private void predict() {
        if (classifier == null) return;

        // Measure inference time (shows the benefit of on-device ML)
        long start = System.nanoTime();
        float[] probs = classifier.classify(drawingView.getBitmap());
        long timeMs = (System.nanoTime() - start) / 1_000_000;

        if (probs == null) {
            Toast.makeText(this, "Please draw a digit first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sort digit indices by probability (highest first) to get top-3
        Integer[] idx = new Integer[probs.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Float.compare(probs[b], probs[a]));

        int best = idx[0];
        float bestConf = probs[best] * 100f;

        tvResult.setText("Prediction: " + best);
        tvTime.setText("Inference time: " + timeMs + " ms");

        for (int i = 0; i < 3; i++) {
            float conf = probs[idx[i]] * 100f;
            labels[i].setText(String.format(Locale.US, "Digit %d  -  %.1f%%", idx[i], conf));
            bars[i].setProgress(Math.round(conf));
        }

        // Add to history (newest on top)
        historyCount++;
        history.insert(0, String.format(Locale.US, "#%d -> %d (%.1f%%)\n", historyCount, best, bestConf));
        tvHistory.setText(history.toString());
    }

    /** Clears the canvas and the result displays. */
    private void clearAll() {
        drawingView.clear();
        tvResult.setText("Prediction: -");
        tvTime.setText("Inference time: - ms");
        for (int i = 0; i < 3; i++) {
            labels[i].setText("");
            bars[i].setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close(); // free model memory
    }
}