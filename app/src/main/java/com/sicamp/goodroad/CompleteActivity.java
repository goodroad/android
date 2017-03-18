package com.sicamp.goodroad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompleteActivity extends AppCompatActivity {

    @BindView(R.id.imageView10) ImageView mImage;
    @BindView(R.id.textView13) TextView mTextTime;
    @BindView(R.id.textView14) TextView mTextLocation;
    @BindView(R.id.textView15) TextView mTextGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        ButterKnife.bind(this);

        Picasso.with(this).load(R.drawable.end_page).into(mImage);

        Intent intent = getIntent();
        mTextTime.setText(intent.getStringExtra("time"));
        mTextLocation.setText(intent.getStringExtra("location"));
        mTextGroup.setText(intent.getStringExtra("group"));

    }
}
