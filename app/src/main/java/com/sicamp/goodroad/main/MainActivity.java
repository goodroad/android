package com.sicamp.goodroad.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sicamp.goodroad.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.getInstance()).commit();
        }
    }
}
