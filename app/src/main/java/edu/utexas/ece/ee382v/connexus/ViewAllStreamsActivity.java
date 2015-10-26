package edu.utexas.ece.ee382v.connexus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.utexas.ece.ee382v.connexus.connexus.R;

public class ViewAllStreamsActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllStreamsActivity";

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

    }
}
