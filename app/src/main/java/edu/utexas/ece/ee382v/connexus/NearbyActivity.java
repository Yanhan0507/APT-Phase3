package edu.utexas.ece.ee382v.connexus;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import edu.utexas.ece.ee382v.connexus.connexus.R;


public class NearbyActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "NearbyActivity";

    final String request_ws_url = "http://ee382v-apt-connexus.appspot.com/ws/stream/m_view_nearby_photos/";

    LocationManager mLocationManager;

    Context context = this;

    private static String usr_email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                /* Get the usr_email from the bundle */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usr_email = extras.getString("usr_email");
            if(!usr_email.isEmpty()){
                Log.d(TAG, "NearbyActivity>>onCreate() Got usr_email: " + usr_email);
            }
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
            onLocationChanged(location);
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }


    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);

            String loc_str = location.getLatitude() + "_" + location.getLongitude();
            /* Get the location. Now update the nearby images */
            updateNearbyImagesAsync(loc_str, 0);

        }
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

    private void updateNearbyImagesAsync(String loc_str, int start_idx){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("loc_str", loc_str);

        httpClient.addHeader("Accept", "application/json");
        Log.d(TAG, "Sending out the request with param" + params.toString());
        httpClient.get(request_ws_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                /*
                 * Return formats: stream_owner, stream_name, img_id_lst, img_url_lst,
                 * upload_url, last_idx, nrof_imgs_in_stream
                 * */

                final ArrayList<String> img_url_lst = new ArrayList<String>();
                final ArrayList<String> distance_lst = new ArrayList<String>();
                final ArrayList<String> stream_id_lst = new ArrayList<String>();

                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray j_img_url_lst = jObject.getJSONArray("img_url_lst");
                    JSONArray j_distance_lst = jObject.getJSONArray("distance_lst");
                    JSONArray j_stream_id_lst = jObject.getJSONArray("stream_id_lst");

                    for (int i = 0; i < j_img_url_lst.length(); i++) {
                        img_url_lst.add(j_img_url_lst.getString(i));
                        distance_lst.add(j_distance_lst.getString(i));
                        stream_id_lst.add(j_stream_id_lst.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.grid_single_stream_view);
                    gridview.setAdapter(new SquareImageAdapter(context, img_url_lst, distance_lst));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            start_viewStreamActivity(stream_id_lst.get(position));

                        }
                    });
                } catch (JSONException j) {
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }


        });

    }

    /* Start a new View stream activity */
    private void start_viewStreamActivity(String stream_id){
        /* Create a new activity */
        Intent intent= new Intent(this, ViewSingleStreamActivity.class);
                            /* Pass the stream_id and user email to the new intent  */
        intent.putExtra("usr_email", usr_email);
        intent.putExtra("stream_id", stream_id);
        startActivity(intent);
    }
}
