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
import android.widget.Button;
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
    static String usr_email = "";
    static String stream_id = "";
    static String stream_name = "";
    static String upload_url = "";

    private static String stream_owner = "";
    final String request_ws_url = "http://ee382v-apt-connexus.appspot.com/ws/stream/m_view_single_stream";

    static int last_idx=0;
    static int nrof_imgs_in_stream=0;

    static Button prev_btn;
    static Button more_btn;

    static int nrof_imgs_on_view;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_stream);

        prev_btn = (Button)findViewById(R.id.prev_img_btn);
        more_btn = (Button)findViewById(R.id.more_img_btn);

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
        findViewById(R.id.prev_img_btn).setOnClickListener(this);
    }

    private void updateImagesAsync(String a_stream_id, int a_start_idx){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("stream_id", a_stream_id);
        params.put("view_stream_start_idx", a_start_idx);

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
                    nrof_imgs_on_view = j_img_url_lst.length();
                    upload_url = jObject.getString("upload_url");
                    last_idx = jObject.getInt("last_idx");
                    System.out.println("updateImagesAsync:: got last_idx = " + last_idx + " from the response.");
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

                Log.d(TAG, "last_idx="+last_idx+", nrof_imgs_in_stream="+nrof_imgs_in_stream);
                //TODO: add a 'more' button if there is more streams
                if (last_idx < nrof_imgs_in_stream){
                    findViewById(R.id.more_img_btn).setEnabled(true);
                    findViewById(R.id.more_img_btn).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.more_img_btn).setVisibility(View.GONE);
                }
                if (last_idx - 16 >0 ){
                    prev_btn.setEnabled(true);
                    prev_btn.setVisibility(View.VISIBLE);
                    System.out.println("updateImagesAsync:: last_idx = " + last_idx + ", rendering previous button.");
                }else{
                    prev_btn.setVisibility(View.GONE);
                    System.out.println("updateImagesAsync:: last_idx = " + last_idx + ", set previous button to invisible.");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }


        });

    }

    public void open_upload_image(View view){
        Intent intent = new Intent(this, ImageUpload.class);
        //TODO: add upload_url and stream id to this

        intent.putExtra("stream_name", stream_name);
        intent.putExtra("stream_id",stream_id );
        intent.putExtra("usr_email",usr_email);

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
                updateImagesAsync(stream_id, last_idx);
                break;
            case R.id.prev_img_btn:
                int start_idx = last_idx - nrof_imgs_on_view - 16;
                if(start_idx<0)
                    start_idx=0;
                updateImagesAsync(stream_id, start_idx);
                break;
        }
    }

}