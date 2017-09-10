package com.lrnplex.droideyes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;*/
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.vision.v1.model.Image;

import static android.content.ContentValues.TAG;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends Activity implements OnInitListener {

    private int cameraId = 0;
    private Camera mCamera;
    private CameraPreview mPreview;
    String fileName = "tempImage.jpeg";
    File file;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyC47K4mjzptir9EhORgAZlAKNJzHMCvn2I";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

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

    private void speakWords(String speech) {

        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);

    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
        if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE) myTTS.setLanguage(Locale.US);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    class MyTimerTask extends TimerTask {
        public void run() {

            try {
                // Call startPreview before taking a picture
                mCamera.startPreview();
                mCamera.takePicture(null, null, null, mPictureCallback);

                //file = new File(getFilesDir(), fileName);
            } catch (NullPointerException ne) {
                ne.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {

            if (imageData != null) {
                //FileOutputStream outputStream = null;
                try {
                    // Create options to help use less memory
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inPreferredConfig = Bitmap.Config.RGB_565;

                    //callCloudVision(BitmapFactory.decodeByteArray(imageData, 0, imageData.length, opt));
                    callCloudVision(imageData);


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }

            }
        }
    };


    private void callCloudVision(final byte[] data) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://vision.googleapis.com/v1/images:annotate?key=" + CLOUD_VISION_API_KEY;

        JSONObject json1= new JSONObject();
        json1.put("type", "LABEL_DETECTION");
        json1.put("maxResults", 1);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json1);

        JSONObject json3= new JSONObject();
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        Log.d("callCloudVision", String.valueOf(data.length));
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes);
        json3.put("content", String.valueOf(base64EncodedImage).substring(9, String.valueOf(base64EncodedImage).length()-1 ));

        JSONObject json2= new JSONObject();
        json2.put("features", jsonArray);
        json2.put("image", json3);

        JSONArray jsonArray2 = new JSONArray();
        jsonArray2.put(json2);

        JSONObject jsonFinal= new JSONObject();
        jsonFinal.put("requests", jsonArray2);

        Log.d("jsonFinal", String.valueOf(jsonFinal));

        String response = POST(url, jsonFinal);

        Log.d("Final Response Cloud", response);

        // Request a string response from the provided URL.
       /* JsonObjectRequest request_json = new JsonObjectRequest(url, jsonFinal,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Process os success response

                        Log.d("Cloud Response", String.valueOf(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error);
                Log.d("Cloud error", String.valueOf(error));
            }
        });

        queue.add(request_json);*/

    }

    private String POST(final String url, final JSONObject jsonObject) {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                InputStream inputStream = null;
                String result = "";
                try {

                    // 1. create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();
                    Log.d("Errrrrr", "1");

                    // 2. make POST request to the given URL
                    HttpPost httpPost = new HttpPost(url);
                    Log.d("Errrrrr", "2");
                    String json;

                    // 3. build jsonObject

                    // 4. convert JSONObject to JSON to String
                    json = jsonObject.toString();
                    Log.d("Errrrrr", "4");

                    // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                    // ObjectMapper mapper = new ObjectMapper();
                    // json = mapper.writeValueAsString(person);

                    // 5. set json to StringEntity
                    StringEntity se = new StringEntity(json);
                    Log.d("Errrrrr", "5");

                    // 6. set httpPost Entity
                    httpPost.setEntity(se);
                    Log.d("Errrrrr", "6");

                    // 7. Set some headers to inform server about the type of the content
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");
                    Log.d("Errrrrr", "7");

                    // 8. Execute POST request to the given URL
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    Log.d("Errrrrr", "8");

                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();
                    Log.d("Errrrrr", "9");

                    // 10. convert inputstream to string
                    if (inputStream != null)
                        result = convertInputStreamToString(inputStream);
                    else
                        result = "Did not work!";

                    Log.d("Errrrrr", "10 - Finished");

                } catch (Exception e) {
                    Log.d("InputStream", e.getMessage());
                }

                // 11. return result
                Log.d("Finish",result);
                speakWords(result);
                return result;
            }
            protected void onPostExecute(String result) {
                //mImageDetails.setText(result);
                Log.d("callCloudVision", "onPostExecute: " + result);

                //speakWords(result);
            }
        }.execute();
        return "...";
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

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