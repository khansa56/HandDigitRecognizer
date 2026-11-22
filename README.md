<div align="center">

# ✍️ Handwritten Digit Recognizer

### On-device AI for Android with TensorFlow Lite

Draw a digit (0-9) with your finger and a CNN recognizes it instantly, **fully offline**, in a few milliseconds.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/TensorFlow%20Lite-2.17.0-FF6F00?logo=tensorflow&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)
![Accuracy](https://img.shields.io/badge/MNIST%20test%20accuracy-99.30%25-brightgreen)
![Model Size](https://img.shields.io/badge/Model%20size-228%20KB-informational)

</div>

---

## 📖 Overview

**Handwritten Digit Recognizer** is a mini AI application built for *Assignment No. 2: AI-Based Mini Application Development (Integrating ML/DL Models into Android with TensorFlow Lite)*.

The user draws a digit on a touch canvas. The drawing is converted into the format the model was trained on, passed to a TensorFlow Lite model running **locally on the phone**, and the result is shown with confidence bars. No internet connection and no server are needed, and the drawing never leaves the device.

The project extends the reference repository [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite): the reference app feeds **one number** to a tiny linear model, while this app feeds a **28×28 image** to a Convolutional Neural Network trained on MNIST. The integration pattern is the same (model in `assets`, `Interpreter`, input buffer, output buffer, result in the UI).

## ✨ Features

| Feature | Description |
| --- | --- |
| 🖊️ Drawing canvas | Free-hand drawing with a thick white brush on a black canvas (the same format as MNIST) |
| ⚡ On-device inference | TensorFlow Lite runs the CNN locally, works offline |
| 🥇 Top-3 predictions | The three most likely digits with percentage and a confidence bar |
| ⏱️ Inference time | Prediction time displayed in milliseconds |
| 🧹 Clear button | Resets the canvas and all results |
| 🕘 Prediction history | Previous predictions with confidence, newest first |
| 🛡️ Empty canvas check | Friendly message if you press Predict without drawing |

## 📸 Screenshots

| Home screen | Digit 2 (100.0%) | Digit 1 (99.4%) | Digit 0 (89.1%) |
| :---: | :---: | :---: | :---: |
| <img src="screenshots/home.png" width="200"/> | <img src="screenshots/digit2.png" width="200"/> | <img src="screenshots/digit1.png" width="200"/> | <img src="screenshots/digit0.png" width="200"/> |

The screenshots show the top-3 predictions with confidence bars, the inference time (5-23 ms on a real phone) and the prediction history.

## 🧠 How It Works

```
 Finger drawing ─▶ Bitmap ─▶ Scale to 28×28 ─▶ Grayscale 0-255 ─▶ ÷ 255 (0..1)
                                                                      │
 UI: digit, top-3, time ◀─ Sort probabilities ◀─ float[1][10] ◀─ Interpreter ◀─ ByteBuffer [1,28,28,1]
```

1. `DrawingView` records finger strokes into an off-screen bitmap (white on black).
2. `DigitClassifier` scales the bitmap to **28×28**, reads each pixel's grayscale value, and normalizes it to **0-1**.
3. The values are written into a `float32` `ByteBuffer` of shape **`[1, 28, 28, 1]`**.
4. `interpreter.run(input, output)` fills a `float[1][10]` array with one probability per digit (softmax).
5. `MainActivity` sorts the probabilities, shows the best digit, the top-3 bars, the inference time, and adds the result to the history.

## 🤖 The Model

Trained in Google Colab on the **MNIST** dataset (60,000 training and 10,000 test images, 28×28 grayscale) and converted to TensorFlow Lite with dynamic-range quantization.

| Layer | Output shape | Parameters |
| --- | --- | ---: |
| Conv2D (32, 3×3, ReLU) | (26, 26, 32) | 320 |
| MaxPooling2D | (13, 13, 32) | 0 |
| Conv2D (64, 3×3, ReLU) | (11, 11, 64) | 18,496 |
| MaxPooling2D | (5, 5, 64) | 0 |
| Flatten | 1600 | 0 |
| Dense (128, ReLU) | 128 | 204,928 |
| Dropout (0.3) | 128 | 0 |
| Dense (10, softmax) | 10 | 1,290 |
| **Total** | | **225,034** |

| Metric | Value |
| --- | --- |
| Optimizer / Loss | Adam / sparse categorical cross-entropy |
| Epochs / Batch size | 8 / 128 |
| Final training accuracy | **99.41%** |
| Final test accuracy | **99.30%** |
| Final validation loss | 0.0223 |
| Keras model size | 879.04 KB |
| TFLite model size | **228.07 KB** (about 3.9× smaller) |

The full training and conversion code is in [`MNIST_Digit_Model.ipynb`](MNIST_Digit_Model.ipynb).

## 🧰 Tech Stack

- **Language:** Java (Android), Python (training)
- **IDE:** Android Studio
- **ML:** TensorFlow / Keras, TensorFlow Lite 2.17.0 (`org.tensorflow:tensorflow-lite`)
- **Training platform:** Google Colab
- **Dataset:** MNIST
- **UI:** custom `View` (`DrawingView`), `ProgressBar`, `TextView`, `Button`
- **SDK:** minSdk 24, targetSdk 37

## 📁 Project Structure

```
DigitRecognizer/
├── app/
│   ├── build.gradle.kts                    # dependencies, noCompress for .tflite
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── mnist.tflite                # trained TensorFlow Lite model
│       ├── java/com/example/digitrecognizer/
│       │   ├── MainActivity.java           # UI logic, buttons, results, history
│       │   ├── DrawingView.java            # custom drawing canvas
│       │   └── DigitClassifier.java        # model loading, preprocessing, inference
│       └── res/layout/
│           └── activity_main.xml           # screen layout
├── MNIST_Digit_Model.ipynb                 # Colab notebook: training + TFLite conversion
├── screenshots/                            # images used in this README
└── README.md
```

## ✅ Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable version)
- JDK 11 or newer (bundled with Android Studio)
- [Git](https://git-scm.com/)
- An Android phone (Android 7.0 / API 24 or higher) with USB debugging enabled, **or** an Android emulator

## 🚀 Installation and Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   ```
2. **Open the project:** in Android Studio choose **File → Open** and select the cloned folder. Wait for Gradle sync to finish.
3. **Check the model:** make sure `app/src/main/assets/mnist.tflite` exists.
4. **Select a device:**
   - *Phone:* enable Developer options → USB debugging, connect by USB and allow the prompt.
   - *Emulator:* open **Device Manager → Create Device** (for example Pixel 6) and start it.
5. **Build and run:** choose **Build → Clean Project**, then press the green **Run ▶** button.
6. **Use the app:** draw a digit **large and centered** in the black box and press **Predict**. Press **Clear** to try again.

## 📦 Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    // AndroidX AppCompat, Material, ConstraintLayout (from the project template)
}

android {
    // Keep the model uncompressed so it can be memory-mapped
    androidResources {
        noCompress += "tflite"
    }
}
```

## 🔍 Code Highlights

**Loading the model** (`DigitClassifier.java`): the `.tflite` file is memory-mapped from `assets` and given to the `Interpreter`.

```java
AssetFileDescriptor fd = context.getAssets().openFd("mnist.tflite");
FileChannel channel = new FileInputStream(fd.getFileDescriptor()).getChannel();
MappedByteBuffer model = channel.map(FileChannel.MapMode.READ_ONLY,
        fd.getStartOffset(), fd.getDeclaredLength());
Interpreter interpreter = new Interpreter(model);
```

**Running inference:** input `[1, 28, 28, 1]` float32, output `[1, 10]` probabilities.

```java
float[][] output = new float[1][10];
interpreter.run(inputBuffer, output);
```

## 💡 Tips for Best Results

- Draw the digit **big and in the center** of the canvas. MNIST digits are centered and fairly thick.
- Draw **one digit at a time**.
- Similar shapes such as **4 and 9**, or **3 and 5**, are the most likely to be confused. Try again with a clearer stroke if the confidence is low.

## 🛠️ Troubleshooting

| Problem | Fix |
| --- | --- |
| "Model loading failed" | `mnist.tflite` must be inside `app/src/main/assets/` with exactly this name |
| `Namespace 'org.tensorflow.lite.support' is used in multiple modules` | Remove the `tensorflow-lite-support` and `tensorflow-lite-metadata` dependencies and keep only `tensorflow-lite` |
| Wrong predictions | Draw larger, thicker and centered |
| `R` shown in red | **Build → Clean Project**, then sync Gradle |
| "No Devices" | Connect a phone with USB debugging or create an emulator in Device Manager |

## 🔮 Future Improvements

- Automatic cropping and centering of the drawing before inference
- Multi-digit recognition
- GPU / NNAPI delegate for even faster inference
- Dark theme and landscape layout

## 🙏 Credits and Acknowledgements

- Reference project: [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite) by Uzair390-Del, used to understand how a TFLite model is loaded and run in Android.
- Dataset: [MNIST handwritten digits database](http://yann.lecun.com/exdb/mnist/) by Y. LeCun, C. Cortes and C. Burges.
- Frameworks: [TensorFlow / Keras](https://www.tensorflow.org) and [TensorFlow Lite (LiteRT)](https://ai.google.dev/edge/litert).
- Training environment: [Google Colab](https://colab.research.google.com).

## 👤 Author

**khansa javeed**
 Assignment No. 2

---

<div align="center">

⭐ If you found this project useful, consider giving it a star.

</div>
