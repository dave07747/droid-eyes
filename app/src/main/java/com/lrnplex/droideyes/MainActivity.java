package com.lrnplex.droideyes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private int cameraId = 0;
    private Camera mCamera;
    private CameraPreview mPreview;
    String fileName = "tempImage.jpeg";
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance(cameraId);

        if (mCamera == null) {
            Toast.makeText(
                    getApplicationContext(),
                    "The camera service is currently unavailable, please try again!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            // Create our Preview view and set it as the content of our
            // activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
            frameLayout.addView(mPreview);

        }

        // start thread for these

        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();

        myTimer.schedule(myTask, 3000, 1500);

    }

    class MyTimerTask extends TimerTask {
        public void run() {

            try {
                // Call startPreview before taking a picture
                mCamera.startPreview();
                mCamera.takePicture(null, null, null, mPictureCallback);
                file = new File(getFilesDir(), fileName);
            } catch (NullPointerException ne) {
                ne.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("Droid-Eyes", "timer****");

        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {
            Log.e("Callback TAG", "Here in jpeg Callback");

            if (imageData != null) {
                FileOutputStream outputStream = null;
                try {
                    outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                    outputStream.write(imageData);
                    // Removed the finish call you had here




                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) try {
                        outputStream.close();
                    } catch (IOException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                }

            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                c = Camera.open(cameraId);
            } else {
                c = Camera.open();
            }
        } catch (Exception e) {
            c = null;
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

}