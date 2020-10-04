package com.example.d_vision;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    String fileName;
    boolean permissionToRecordAccepted = false;
    int counter = 3;

    ImageView imageView;
    Interpreter interpreter;
    final float IMAGE_MEAN = 127.5f;
    final float IMAGE_STD = 127.5f;
    final int IMAGE_SIZE_X = 224;
    final int IMAGE_SIZE_Y = 224;
    final int DIM_BATCH_SIZE = 1;
    final int DIM_PIXEL_SIZE = 3;
    final int NUM_BYTES_PER_CHANNEL = 4;
    final int NUM_CLASS = 1001;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionToRecordAccepted = true;

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);


                }


                break;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.img);
        FirebaseApp.initializeApp(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //check if permission request is necessary
        {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {

                permissionToRecordAccepted = true;
                new ImagePicker.Builder(MainActivity.this)
                        .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                        .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                        .directory(getExternalCacheDir().getAbsolutePath())
                        .extension(ImagePicker.Extension.PNG)
                        .scale(600, 600)
                        .allowMultipleImages(false)
                        .enableDebuggingMode(true)
                        .build();



// Multiple object detection in static images
//                ObjectDetectorOptions options =
//                        new ObjectDetectorOptions.Builder()
//                                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                                .enableMultipleObjects()
//                                .enableClassification()  // Optional
//                                .build();


                // You can use the API that requires the permission.
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

                // You can directly ask for the permission.
            }

        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            final List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);


            FirebaseCustomRemoteModel remoteModel =
                    new FirebaseCustomRemoteModel.Builder("yolo").build();
            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                    .requireWifi()
                    .build();
            FirebaseModelManager.getInstance().download(remoteModel, conditions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            final int height = displayMetrics.heightPixels;
                            final int width = displayMetrics.widthPixels;
                            // Download complete. Depending on your app, you could enable
                            // the ML feature, or switch from the local model to the remote
                            // model, etc.
                            FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder("yolo").build();
                            FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                                    .addOnCompleteListener(new OnCompleteListener<File>() {
                                        @Override
                                        public void onComplete(@NonNull Task<File> task) {
                                            File modelFile = task.getResult();
                                            if (modelFile != null) {
                                                interpreter = new Interpreter(modelFile);
                                                Bitmap  bitmap;
                                                try {
                                                   bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.fromFile(new File(mPaths.get(0))));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                                                }

                                                int[] intValues = new int[IMAGE_SIZE_X * IMAGE_SIZE_Y];
                                                bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth()/2, bitmap.getHeight()/2);
bitmap.getP
                                                ByteBuffer imgData =
                                                        ByteBuffer.allocateDirect(
                                                                DIM_BATCH_SIZE
                                                                        * IMAGE_SIZE_X
                                                                        * IMAGE_SIZE_Y
                                                                        * DIM_PIXEL_SIZE
                                                                        * NUM_BYTES_PER_CHANNEL);
                                                imgData.rewind();

// Float model.
                                                int pixel = 0;
                                                for (int i = 0; i < IMAGE_SIZE_X; ++i) {
                                                    for (int j = 0; j < IMAGE_SIZE_Y; ++j) {
                                                        int pixelValue = intValues[pixel++];
                                                        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                                        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                                        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                                    }
                                                }

// Quantized model.
// Output label probabilities.
                                                float[][] labelProbArray = new float[1][NUM_CLASS];

// Run the model.
                                                interpreter.run(imgData, labelProbArray);
                                               // Log.e("Main Activity",labelProbArray[1][0]+"");


                                            }
                                        }
                                    });
                        }
                    });


            //Your Code
        }

    }
}