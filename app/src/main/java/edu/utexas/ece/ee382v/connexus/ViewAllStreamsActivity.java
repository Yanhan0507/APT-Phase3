package edu.utexas.ece.ee382v.connexus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;


import org.json.JSONException;


import edu.utexas.ece.ee382v.connexus.connexus.R;



public class ViewAllStreamsActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllStreamsActivity";

    final String service_url = "/ws/stream/view_all";
    final String request_ws_url = R.string.ws_base_url+service_url;

    private static String usr_email = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_streams);

        /* Get the usr_email from the bundle */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usr_email = extras.getString("usr_email");
            if(!usr_email.isEmpty()){
                Log.d(TAG, "ViewAllStreamsActivity>>onCreate() Got usr_email: " + usr_email);
            }
        }

        /* Get all streams */
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_ws_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
//                final ArrayList<String> imageURLs = new ArrayList<String>();
//                final ArrayList<String> imageCaps = new ArrayList<String>();
                try {
//                    JSONObject jObject = new JSONObject(new String(response));
//                    JSONArray displayImages = jObject.getJSONArray("displayImages");
//                    JSONArray displayCaption = jObject.getJSONArray("imageCaptionList");
//
//                    for(int i=0;i<displayImages.length();i++) {
//
//                        imageURLs.add(displayImages.getString(i));
//                        imageCaps.add(displayCaption.getString(i));
//                        System.out.println(displayImages.getString(i));
//                    }
//                    GridView gridview = (GridView) findViewById(R.id.gridview);
//                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
//                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View v,
//                                                int position, long id) {
//
//                            Toast.makeText(context, imageCaps.get(position), Toast.LENGTH_SHORT).show();
//
//                            Dialog imageDialog = new Dialog(context);
//                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                            imageDialog.setContentView(R.layout.thumbnail);
//                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);
//
//                            Picasso.with(context).load(imageURLs.get(position)).into(image);
//
//                            imageDialog.show();
//                        }
//                    });
                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }


        /* Enable view subscribed streams button if the user has logged in */


    }
}
