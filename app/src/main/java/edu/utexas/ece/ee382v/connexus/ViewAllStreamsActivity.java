package edu.utexas.ece.ee382v.connexus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.utexas.ece.ee382v.connexus.connexus.R;



public class ViewAllStreamsActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "ViewAllStreamsActivity";

//    final String service_url = "ws/stream/view_all";
//    final String request_ws_url = R.string.ws_base_url+service_url;
    final String request_ws_url = "http://ee382v-apt-connexus.appspot.com/ws/stream/view_all";

    private static String usr_email = "";
//    private static String usr_id = "";
    Context context = this;

    private int last_stream_idx = -1;
    private boolean is_view_subscribed = false;

    Button subscribed_btn, nearby_btn;

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

        updateStreamsAsync(false,usr_email,0);

        subscribed_btn = (Button) findViewById(R.id.subscribed_stream_btn);

        nearby_btn = (Button) findViewById(R.id.nearby_btn);

        /* Enable view subscribed streams button if the user has logged in */
        if(!usr_email.isEmpty()){
            subscribed_btn.setVisibility(View.VISIBLE);
            subscribed_btn.setOnClickListener(this);
            nearby_btn.setOnClickListener(this);

        }else{
            subscribed_btn.setVisibility(View.GONE);
        }


    }


    private void updateStreamsAsync(boolean is_view_subscribed, String usr_id, int start_idx){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("view_all_start_idx", start_idx);
        if (!is_view_subscribed){
            /* get all streams */
            Log.d(TAG, "Switching to view all streams");
        }else{
            /* get subscribed streams */
            params.put("is_view_all_subscribed", true);
            params.put("user_email", usr_email);
            params.put("view_all_start_idx", is_view_subscribed);

            Log.d(TAG, "Switching to view subscribed streams");
        }
        httpClient.addHeader("Accept", "application/json");
        Log.d(TAG, "Sending out the request with param" + params.toString());
        httpClient.get(request_ws_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> stream_id_lst = new ArrayList<String>();
                final ArrayList<String> stream_name_lst = new ArrayList<String>();
                final ArrayList<String> cover_img_url_list = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jStreamIdLst = jObject.getJSONArray("stream_id_lst");
                    JSONArray jStreamNameLst = jObject.getJSONArray("stream_name_lst");
                    JSONArray jCoverLst = jObject.getJSONArray("cover_img_url_list");
                    last_stream_idx = jObject.getInt("last_idx");

                    for (int i = 0; i < jStreamIdLst.length(); i++) {
                        stream_id_lst.add(jStreamIdLst.getString(i));
                        stream_name_lst.add(jStreamNameLst.getString(i));
                        cover_img_url_list.add(jCoverLst.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context, cover_img_url_list));
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

        //TODO: add a 'more' button if there is more streams

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

    private void onSubscribedBtnClicked() {
        if (!is_view_subscribed) {
            /* Changing to view all*/
            subscribed_btn.setText(R.string.view_all_btn);
            is_view_subscribed = true;
        }
        else{
            subscribed_btn.setText(R.string.subscribed_stream_btn);
            is_view_subscribed = false;
        }
        updateStreamsAsync(is_view_subscribed, usr_email, 0);
    }


    // [START on_click]
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.subscribed_stream_btn:
                onSubscribedBtnClicked();
                break;
            case R.id.nearby_btn:
                Intent intent= new Intent(this, NearbyActivity.class);
                /* Pass the stream_id and user email to the new intent  */
                intent.putExtra("usr_email", usr_email);
                startActivity(intent);
                break;
        }
    }
    // [END on_click]
}
