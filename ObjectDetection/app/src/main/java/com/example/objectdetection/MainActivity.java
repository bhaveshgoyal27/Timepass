package com.example.objectdetection;

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
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    String fileName;
    boolean permissionToRecordAccepted = false;
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




                    Log.e("MainActivity","yrs34234");
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    Bitmap bitmap,bitmap1;


                    try {
                        bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.fromFile(new File(mPaths.get(0))));
                        bitmap=Bitmap.createBitmap(bitmap1.getWidth(),bitmap1.getHeight(),Bitmap.Config.ARGB_8888);

                    } catch (IOException e) {
                        e.printStackTrace();
                        bitmap1=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                        bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

                    }





            ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).build();
            ObjectDetector objectDetector = null;
            try {
                objectDetector = ObjectDetector.createFromFileAndOptions(getApplicationContext(), "mnasnet_1.3_224_1_metadata_1.tflite", options);
                Log.e("MainActivity","yes ! yes!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MainActivity",e.getMessage());
            }
//
//            ImageProcessor imageProcessor =
//                    new ImageProcessor.Builder()
//                            .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//                            .build();
            TensorImage image1=new TensorImage(DataType.UINT8);
            image1.load(bitmap1);



            Canvas canvas=new Canvas(bitmap);

            Paint paint=new Paint();

            paint.setColor(Color.RED);

            paint.setStyle(Paint.Style.STROKE);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
// Run inference
            List<Detection> results = objectDetector.detect(image1);

            for (Detection detection : results)
            {
                RectF rectF= detection.getBoundingBox();
                canvas.drawRect(rectF,paint);



            }

            canvas.drawBitmap(bitmap1,0,0,paint);

            imageView.setImageBitmap(bitmap);





            Log.e("MainActivity",results.toString());


                }

            //Your Code
        }

    }




