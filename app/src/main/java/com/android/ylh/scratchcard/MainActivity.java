package com.android.ylh.scratchcard;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity {
    Scratchcard mScratchcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mScratchcard = (Scratchcard) findViewById(R.id.scratchcard);
        mScratchcard.setOnScratchcardListener(new Scratchcard.OnScratchcardListener() {
            @Override
            public void complete() {
                Toast.makeText(getApplicationContext(), "gogogogo", Toast.LENGTH_LONG).show();
            }

        });
        mScratchcard.setText("ggggggg");
    }


}
