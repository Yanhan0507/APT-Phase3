package edu.utexas.ece.ee382v.connexus;



import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import edu.utexas.ece.ee382v.connexus.connexus.R;

//import android.support.v7.app.AppCompatActivity;


public class ImageUpload extends AppCompatActivity implements LocationListener {

    private static final String TAG = "NearbyActivity";

    final String request_addr_get_upload_url ="http://ee382v-apt-connexus.appspot.com/ws/stream/m_get_upload_url";

    LocationManager mLocationManager;

    Location location;

    static String streamName = "";
    static String streamID = "";
    static String email = "";


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int PICK_IMAGE = 1;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);


        streamName = getIntent().getStringExtra("stream_name");
        streamID = getIntent().getStringExtra("stream_id");
        email = getIntent().getStringExtra("usr_email");

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
            onLocationChanged(location);
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        // Choose image from library
        Button chooseFromLibraryButton = (Button) findViewById(R.id.choose_from_library);
        chooseFromLibraryButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        // To do this, go to AndroidManifest.xml to add permission
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Start the Intent
                        startActivityForResult(galleryIntent, PICK_IMAGE);
                    }
                }
        );

    }


    public void createCameraPreview(View view){
        if (checkCameraHardware(this)==true){
            Intent intent = new Intent(this,CameraActivity.class);
            startActivityForResult(intent, 2);
        }
    }


    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.image_upload, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();

            // User had pick an image.

            String[] filePathColumn = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            // Link to the image

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imageFilePath = cursor.getString(columnIndex);
            cursor.close();

            // Bitmap imaged created and show thumbnail

            ImageView imgView = (ImageView) findViewById(R.id.thumbnail);
            final Bitmap bitmapImage = BitmapFactory.decodeFile(imageFilePath);
            imgView.setImageBitmap(bitmapImage);

            // Enable the upload button once image has been uploaded

            Button uploadButton = (Button) findViewById(R.id.upload_to_server);
            uploadButton.setClickable(true);

            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get rphoto caption

                            EditText text = (EditText) findViewById(R.id.upload_message);
                            String photoCaption = text.getText().toString();


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();
                            String location_str = location.getLatitude() + "_" + location.getLongitude();

                            getUploadURL(b, photoCaption,location_str);
                        }
                    }
            );
        }
        else {
            String imagefilepath = data.getStringExtra("imageFile");

            ImageView imgView = (ImageView) findViewById(R.id.thumbnail);
            final Bitmap bitmapImage = BitmapFactory.decodeFile(imagefilepath);
            imgView.setImageBitmap(bitmapImage);

            // Enable the upload button once image has been uploaded

            Button uploadButton = (Button) findViewById(R.id.upload_to_server);
            uploadButton.setClickable(true);

            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            EditText text = (EditText) findViewById(R.id.upload_message);
                            String photoCaption = text.getText().toString();


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();
//                            String location=mLastLocation.getLatitude()+"_"+mLastLocation.getLongitude();
                            String location_str = location.getLatitude() + "_" + location.getLongitude();

                            getUploadURL(b, photoCaption,location_str);
                        }
                    }
            );


        }
    }

    private void getUploadURL(final byte[] encodedImage, final String photoCaption,final String location){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.addHeader("Accept", "application/json");
        Log.d(TAG, "Sending out the request for upload_url");
        httpClient.get(request_addr_get_upload_url, new AsyncHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String upload_url;
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    upload_url = jObject.getString("upload_url");
                    postToServer(encodedImage, photoCaption, upload_url, location);
                } catch (JSONException j) {
                    System.out.println("JSON Error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Get_serving_url", "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    private void postToServer(byte[] encodedImage,String description, String the_upload_url, String location){
        System.out.println(the_upload_url);
        RequestParams params = new RequestParams();
        params.put("file",new ByteArrayInputStream(encodedImage));
        params.put("stream_description", description);
        params.put("location", location);

        params.put("stream_name", streamName);
        params.put("stream_id", streamID);
        params.put("usr_email", email);


        // stream_id
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.post(the_upload_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("async", "success!!!!");
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ImageUpload.this, ViewSingleStreamActivity.class);

                intent.putExtra("stream_name", streamName);
                intent.putExtra("stream_id", streamID);
                intent.putExtra("usr_email", email);

                startActivity(intent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Posting_to_blob", "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            Log.d("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);

            String loc_str = location.getLatitude() + "_" + location.getLongitude();
            /* Get the location. Now update the nearby images */


        }
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}


    public void viewAllImages(View view){
        Intent intent= new Intent(this, ViewAllStreamsActivity.class);
        startActivity(intent);
    }


}
