package edu.utexas.ece.ee382v.connexus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.utexas.ece.ee382v.connexus.connexus.R;

public class ViewSingleStreamActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ViewSingleStreamAct";

    /* data members */
    private static String usr_email = "";
    private static String stream_id = "";
    private static String stream_name = "";
    private static String upload_url = "";

    private static String stream_owner = "";
    final String request_ws_url = "http://ee382v-apt-connexus.appspot.com/ws/stream/m_view_single_stream";

    private int last_idx=0;
    private int nrof_imgs_in_stream=0;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_stream);

        /* Get the usr_email and stream_id from the bundle */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usr_email = extras.getString("usr_email");
            stream_id = extras.getString("stream_id");
            if(!usr_email.isEmpty()){
                Log.d(TAG, "ViewAllStreamsActivity>>onCreate() Got usr_email: " + usr_email);
            }
            if(!stream_id.isEmpty()){
                Log.d(TAG, "ViewAllStreamsActivity>>onCreate() Got stream_id: " + stream_id);
            }
        }

        updateImagesAsync(stream_id, 0);

        /* Add listeners for buttons */
        findViewById(R.id.upload_img_btn).setOnClickListener(this);
        findViewById(R.id.back_streams_btn).setOnClickListener(this);
        findViewById(R.id.more_img_btn).setOnClickListener(this);
    }

    private void updateImagesAsync(String a_stream_id, int start_idx){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("stream_id", a_stream_id);
        params.put("view_stream_start_idx", start_idx);

        httpClient.addHeader("Accept", "application/json");
        Log.d(TAG, "Sending out the request with param" + params.toString());
        httpClient.get(request_ws_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                /*
                 * Return formats: stream_owner, stream_name, img_id_lst, img_url_lst,
                 * upload_url, last_idx, nrof_imgs_in_stream
                 * */

                final ArrayList<String> img_id_lst = new ArrayList<String>();
                final ArrayList<String> img_url_lst = new ArrayList<String>();

                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    stream_owner = jObject.getString("stream_owner");
                    stream_name = jObject.getString("stream_name");
                    JSONArray j_img_id_lst = jObject.getJSONArray("img_id_lst");
                    JSONArray j_img_url_lst = jObject.getJSONArray("img_url_lst");
                    upload_url = jObject.getString("upload_url");
                    last_idx = jObject.getInt("last_idx");
                    nrof_imgs_in_stream = jObject.getInt("nrof_imgs_in_stream");

                    for (int i = 0; i < j_img_id_lst.length(); i++) {
                        img_id_lst.add(j_img_id_lst.getString(i));
                        img_url_lst.add(j_img_url_lst.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.grid_single_stream_view);
                    gridview.setAdapter(new ImageAdapter(context, img_url_lst));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {


//                            Toast.makeText(context, imageCaps.get(position), Toast.LENGTH_SHORT).show();

                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.view_img_dialog);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.dialog_img_view);

                            Picasso.with(context).load(img_url_lst.get(position)).into(image);

                            imageDialog.show();

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

    public void open_upload_image(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        //TODO: add upload_url and stream id to this
        startActivity(intent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_img_btn:
                open_upload_image(v);
                break;
            case R.id.back_streams_btn:
                super.onBackPressed();
                break;
            case R.id.more_img_btn:
                break;
        }
    }

}