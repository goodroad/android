package com.sicamp.goodroad.splash;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sicamp.goodroad.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_splash, SplashFragment.getInstance()).commit();
        }
    }
}
