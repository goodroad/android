package com.sicamp.goodroad.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sicamp.goodroad.CameraActivity;
import com.sicamp.goodroad.R;
import com.sicamp.goodroad.ReportActivity;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashFragment extends Fragment {
    private static final String TAG = SplashFragment.class.getSimpleName();

    private static SplashFragment mInstance;

    private static long SPLASH_TIME = 3000;

    private Runnable mRunnable;
    private Handler mHandler;

    @BindView(R.id.image_splash) ImageView mImage;

    public static SplashFragment getInstance() {
        if(mInstance == null) {
            mInstance = new SplashFragment();
        }
        return mInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.bind(this, view);
        Picasso.with(getContext()).load(R.drawable.loading_logo).into(mImage);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, SPLASH_TIME);

    }
}
