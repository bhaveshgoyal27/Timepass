package com.example.detectfaces;

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
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String fileName;
    boolean permissionToRecordAccepted = false;
    InputImage image;
    int counter = 3;

    ImageView imageView;

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




            Log.e("MainActivity","yrs34234");








            FaceDetectorOptions highAccuracyOpts =
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .build();

            try {
                image = InputImage.fromFilePath(getApplicationContext(),Uri.fromFile(new File(mPaths.get(0))) );
            } catch (IOException e) {
                e.printStackTrace();
            }

            FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

            Task<List<Face>> result =
                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            Bitmap bitmap,bitmap1;
                                            DisplayMetrics displayMetrics = new DisplayMetrics();
                                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                            int height = displayMetrics.heightPixels;
                                            int width = displayMetrics.widthPixels;

                                            try {
                                                bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(mPaths.get(0))));
                                                bitmap=Bitmap.createBitmap(bitmap1.getWidth(),bitmap1.getHeight(),Bitmap.Config.ARGB_8888);

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                bitmap1=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                                                bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

                                            }


                                            final Canvas canvas=new Canvas(bitmap);

                                            final Paint paint=new Paint();

                                            paint.setColor(Color.RED);

                                            paint.setStyle(Paint.Style.STROKE);
                                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                float rotZ = face.getHeadEulerAngleZ();
                                                // Head is tilted sideways rotZ degrees

                                                canvas.drawRect(bounds,paint);

                                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                // nose available):
                                                FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                                if (leftEar != null) {
                                                    PointF leftEarPos = leftEar.getPosition();
                                                }

                                                // If co
                                                // ntour detection was enabled:
//                                                List<PointF> leftEyeContour =
//                                                        face.getContour(FaceContour.LEFT_EYE).getPoints();
//                                                List<PointF> upperLipBottomContour =
//                                                        face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();

                                                // If classification was enabled:
                                                if (face.getSmilingProbability() != null) {
                                                    float smileProb = face.getSmilingProbability();

                                                    Log.e("Main",smileProb+"");
                                                }
                                                if (face.getRightEyeOpenProbability() != null) {
                                                    float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                }

                                                // If face tracking was enabled:
                                                if (face.getTrackingId() != null) {
                                                    int id = face.getTrackingId();
                                                }
                                            }
                                            canvas.drawBitmap(bitmap1,0,0,paint);

                                            imageView.setImageBitmap(bitmap);
                                            // Task completed successfully
                                            // ...
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });






        }

        //Your Code
    }





}