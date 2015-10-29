package edu.utexas.ece.ee382v.connexus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.utexas.ece.ee382v.connexus.connexus.R;

public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultActivity";

    final String request_ws_url = "http://ee382v-apt-connexus.appspot.com/ws/stream/m_search";

    Context context = this;

    static String search_keyword;
    static String search_type;
    static String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        /* Get the search_keyword, search_type from the bundle */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            search_keyword = extras.getString("search_keyword");
            search_type = extras.getString("search_type");
            search_type = extras.getString("user_email");
            if(!search_keyword.isEmpty()){
                Log.d(TAG, "SearchResultActivity>>onCreate() Got search_keyword: " + search_keyword);
            }
        }

        updateSearchResultAsync(search_keyword, search_type);

    } // end onCreate()

    private void updateSearchResultAsync(String a_keyword, String a_type){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("search_keyword", a_keyword);
        params.put("search_type", a_type);

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

                    for (int i = 0; i < jStreamIdLst.length(); i++) {
                        stream_id_lst.add(jStreamIdLst.getString(i));
                        stream_name_lst.add(jStreamNameLst.getString(i));
                        cover_img_url_list.add(jCoverLst.getString(i));
                    }

                    TextView res_desc = (TextView)findViewById(R.id.search_res_desc);
                    res_desc.setText("Found " + stream_id_lst.size() + " results for the keyword " + search_keyword);
                    GridView gridview = (GridView) findViewById(R.id.search_res_gridview);
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
    }

    /* Start a new View stream activity */
    private void start_viewStreamActivity(String stream_id){
        /* Create a new activity */
        Intent intent= new Intent(this, ViewSingleStreamActivity.class);
                            /* Pass the stream_id and user email to the new intent  */
        intent.putExtra("usr_email", user_email);
        intent.putExtra("stream_id", stream_id);
        startActivity(intent);
    }
}
