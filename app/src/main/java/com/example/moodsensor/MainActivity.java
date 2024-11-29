package com.example.moodsensor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moodsensor.Utils.Utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextView emotionResult;
    private ImageView capturedImage;
    private Interpreter interpreter;
    private boolean isFrontCamera = false;
    private Queue<String> emotionQueue = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        previewView = findViewById(R.id.previewView);
        emotionResult = findViewById(R.id.emotion_result);
        capturedImage = findViewById(R.id.captured_image);
        Button captureButton = findViewById(R.id.capture_button);
        ImageButton switchCameraButton = findViewById(R.id.switch_camera_button);
        Button resetButton = findViewById(R.id.reset_button);

        // Initialize the camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Load TFLite model asynchronously
        loadModelInBackground();

        // Request camera permissions or start the camera
        if (allPermissionsGranted()) {
            startCamera(isFrontCamera);
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        // Set up the capture button click listener
        captureButton.setOnClickListener(v -> captureImage());

        // Set up the switch camera button click listener
        switchCameraButton.setOnClickListener(v -> switchCamera());

        // Set up the reset button click listener
        resetButton.setOnClickListener(v -> resetEmotionDisplay());
    }

    private void loadModelInBackground() {
        new Thread(() -> {
            try {
                MappedByteBuffer model = Utils.loadModelFile(this, "simple_classifier.tflite");
                interpreter = new Interpreter(model);
                Log.d("TFLite", "Model loaded successfully.");
            } catch (IOException e) {
                Log.e("TFLite", "Error loading model", e);
            }
        }).start();
    }

    private void startCamera(boolean useFrontCamera) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = useFrontCamera ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Bitmap bitmap = Utils.imageProxyToBitmap(image);
                        capturedImage.setImageBitmap(bitmap);
                        detectFaceAndEmotion(bitmap);
                        image.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Error capturing image", exception);
                    }
                });
    }

    private void detectFaceAndEmotion(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        detector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        for (Face face : faces) {
                            Rect bounds = face.getBoundingBox();
                            Bitmap faceBitmap = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
                            ByteBuffer inputBuffer = preprocessFaceBitmap(faceBitmap);

                            // Run the model prediction
                            float[][] output = new float[1][7];
                            interpreter.run(inputBuffer, output);

                            // Post-process the output
                            String predictedEmotion = processModelOutput(output);
                            updateEmotionDisplay(predictedEmotion);
                        }
                    } else {
                        emotionResult.setText("No face detected.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FaceDetection", "Error detecting face", e);
                    emotionResult.setText("Error detecting face.");
                });
    }

    private ByteBuffer preprocessFaceBitmap(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 48 * 48 * 1);
        inputBuffer.order(ByteOrder.nativeOrder());

        // Convert bitmap to grayscale and normalize
        for (int y = 0; y < 48; y++) {
            for (int x = 0; x < 48; x++) {
                int pixel = scaledBitmap.getPixel(x, y);
                int gray = (int) (0.3 * Color.red(pixel) + 0.59 * Color.green(pixel) + 0.11 * Color.blue(pixel));
                float normalizedPixel = (gray / 127.5f) - 1.0f;
                inputBuffer.putFloat(normalizedPixel);
            }
        }
        return inputBuffer;
    }

    private String processModelOutput(float[][] output) {
        float threshold = 0.4f;
        float maxProbability = 0;
        int maxIndex = -1;
        String[] emotions = {"Angry", "Disgust", "Fear", "Happy", "Sad", "Surprised", "Neutral"};

        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxProbability && output[0][i] > threshold) {
                maxProbability = output[0][i];
                maxIndex = i;
            }
        }
        return maxIndex != -1 ? emotions[maxIndex] : "Uncertain";
    }

    private void updateEmotionDisplay(String newEmotion) {
        if (emotionQueue.size() >= 5) {
            emotionQueue.poll();
        }
        emotionQueue.offer(newEmotion);

        // Determine the most stable emotion
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String emotion : emotionQueue) {
            frequencyMap.put(emotion, frequencyMap.getOrDefault(emotion, 0) + 1);
        }
        String stableEmotion = Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        emotionResult.setText("Emotion: " + stableEmotion);
    }

    private void switchCamera() {
        isFrontCamera = !isFrontCamera;
        startCamera(isFrontCamera);
    }

    private void resetEmotionDisplay() {
        capturedImage.setImageResource(0);
        emotionResult.setText("Emotion: ");
        emotionQueue.clear();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera(isFrontCamera);
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
