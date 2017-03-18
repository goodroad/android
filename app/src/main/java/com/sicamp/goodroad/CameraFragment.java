package com.sicamp.goodroad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraFragment extends Fragment {
    private static final String TAG = CameraFragment.class.getSimpleName();

    private static CameraFragment mInstance;

    private String mFileName;

    @BindView(R.id.imageView_bg) ImageView mImageBg;

    public static CameraFragment getInstance() {
        if(mInstance == null) {
            mInstance = new CameraFragment();
        }
        return mInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, view);
        Picasso.with(getContext()).load(R.drawable.roadinpeace).into(mImageBg);
        checkpermission();
        checkLocationPermssion();
        return view;
    }

    @OnClick(R.id.imageButton_camera) void moveCamera() {
        Log.d(TAG, "click moveCamera");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);

        resize(data.getData());
    }

    private void resize(Uri uri) {
        //resize
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 640, 360, true);

        //save
        String path = Environment.getExternalStorageDirectory().getPath() + "/GoodRoad/";
        File pathFile = new File(path);
        if(!pathFile.isDirectory()) {
            pathFile.mkdirs();
        }
        mFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(path + mFileName);
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(file);
                resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                resized.recycle();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "resize error : " + e.toString());
            }
        }

        Intent intent = new Intent(getActivity(), ReportActivity.class);
        intent.putExtra("file", file.getPath());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();

    }

    private void checkpermission() {
        Log.d(TAG, "checkpermission!!");
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }


    }

    private void checkLocationPermssion() {
        Log.d(TAG, "checkLocationPermssion!!");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


}
