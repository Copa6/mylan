package com.example.analyttica.skinsense;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.app.ProgressDialog;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.BufferedWriter;
import javax.net.ssl.HttpsURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 1;

    public static final int IMAGE_GALLERY_REQUEST = 20;
    public static final String UPLOAD_URL = "http://192.168.1.3:8000/skin_cancer/detect";
//    public static final String UPLOAD_URL = "http://192.168.1.3:8000";
    public static final String UPLOAD_KEY = "image";
//    public static final String TAG = "MY MESSAGE";

    ImageView browseImageView;
    public Button browseButton;
    public Button captureButton;
    public Button analysisButton;
    private Bitmap yourSelectedImage;
    String filePath;
    private byte[] blob=null;
    Bitmap image = null;

    private String delimiter = "--";
    private String boundary =  "SwA"+Long.toString(System.currentTimeMillis())+"SwA";
    JSONParser jsonParser = new JSONParser();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    // Client-Server - End //////////////////////



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browseButton = (Button) findViewById(R.id.browseButton);

        captureButton = (Button) findViewById(R.id.captureButton);
        analysisButton = (Button) findViewById(R.id.analysisButton);

        browseImageView = (ImageView) findViewById(R.id.browseImageView);

//        //Disable if user has no camera
        if(!hasCamera())
            captureButton.setEnabled(false);

        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (image != null) {
                    sendImage();
                } else {
                    Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    //Check if the user has camera on phone
    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }


    //Launch Camera
    public void launchCamera (View view){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
    }

    //Take image from gallery
    public void lauchGallery (View v){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);

    }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void sendImage() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String encoded_image = getStringImage(image);
//        StringRequest strRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("Response", response);
//                        try {
//                            JSONObject jObj = new JSONObject(response);
//
//                            String message = jObj.getString("cancer");
//                            System.out.print(response);
//                            if (message.equals("0")) {
//                                Toast.makeText(getApplicationContext(), "Not cancerous", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Cancerous", Toast.LENGTH_LONG).show();
//                            }
//                        } catch (JSONException e) {
//                            // JSON error
//                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    }
//                },new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        })
//        {
//            @Override
//            protected Map<String, String> getParams()
//            {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("image", encoded_image);
//                return params;
//            }
//        };
//        queue.add(strRequest);




        Map<String, String> params = new HashMap();
        params.put("image",encoded_image);
        JSONObject json = new JSONObject(params);
        System.out.println("here");
        JsonObjectRequest json_req = new JsonObjectRequest(Request.Method.POST, UPLOAD_URL, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {
                            JSONObject jObj = new JSONObject(response.toString());
                            String message = jObj.getString("cancer");
                            System.out.print(response);
                            if (message.equals("0")) {
                                Toast.makeText(getApplicationContext(), "Not cancerous", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Cancerous", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(json_req);
    }

    private String getPath(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    //If you want image to return
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            //Get the photo
            if(data!=null) {
                Bundle extras = data.getExtras();
//                Uri picUri = data.getData();
//                filePath = getPath(picUri);
                browseImageView = (ImageView) findViewById(R.id.browseImageView);
                if(extras!=null){
                    image = (Bitmap) extras.get("data");
                    browseImageView.setImageBitmap(image);
                }
            }
        }

        if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK){
            //address of the image on the SD card
            if(data!=null) {
                Uri imageUri = data.getData();
                //Declare a stream to read the image data from gallery
                InputStream inputStream;
                if(imageUri!=null) {
                    filePath = getPath(imageUri);
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);

                        Bitmap image = BitmapFactory.decodeStream(inputStream);
                        //show the image to the user
                        browseImageView.setImageBitmap(image);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
