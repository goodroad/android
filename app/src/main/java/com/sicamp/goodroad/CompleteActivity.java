package com.sicamp.goodroad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompleteActivity extends AppCompatActivity {

    @BindView(R.id.imageView10) ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        ButterKnife.bind(this);

        Picasso.with(this).load(R.drawable.demo).into(mImage);

    }
}
