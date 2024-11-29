package com.example.moodsensor.Utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class Utils {

    public static Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        imageProxy.close();  // Ensure proxy is closed after use.
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static void detectFace(Bitmap bitmap, FaceDetectorOptions options, FaceDetectionCallback callback) {
        FaceDetector detector = FaceDetection.getClient(options);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        detector.process(inputImage)
                .addOnSuccessListener(faces -> callback.onDetectionComplete(faces))
                .addOnFailureListener(e -> Log.e("FaceDetection", "Face detection failed", e));
    }

    public interface FaceDetectionCallback {
        void onDetectionComplete(List<Face> faces);
    }
}

