package com.sicamp.goodroad;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportFragment extends Fragment implements LocationListener, OnMapReadyCallback {
    private static final String TAG = ReportFragment.class.getSimpleName();

    private static ReportFragment mInstance;

    private String mFilePath;
    private String mFileName;
    private File mFile;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final String URL = "http://rest.goodroad.co.kr/api/reports";
    private static final String URL_FILE = "http://rest.goodroad.co.kr/upload";

    private LocationManager mLocationManager;
    private long mTime;
    private double mLng;
    private double mLat;
    private String mTimeString;
    private String mTimeReport;
    private String mLocationString;
    private String mGroup1;

    private GoogleMap mGoogleMap;

    private StaggeredGridLayoutManager mStgaggeredGridLayoutManager;

    @BindView(R.id.image_report) ImageView mImageView;
    @BindView(R.id.textView5) TextView mTextTime;
    @BindView(R.id.textView7) TextView mTextLocation;
    @BindView(R.id.imageView5) ImageView mImageMore;
    @BindView(R.id.layout_select) LinearLayout mLayoutSelect;
    @BindView(R.id.imageView6) ImageView mImage1;
    @BindView(R.id.imageView7) ImageView mImage2;
    @BindView(R.id.imageView8) ImageView mImage3;
    @BindView(R.id.imageView9) ImageView mImage4;

    public static ReportFragment getInstance() {
        if(mInstance == null) {
            mInstance = new ReportFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilePath = getArguments().getString("file");
        mFile = new File(mFilePath);
        mFileName = mFile.getName();
        getTime();
        getLocation();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        ButterKnife.bind(this, view);

        getBitmap(mFilePath);
        mTextTime.setText(mTimeString);
        mTextLocation.setText(mLocationString);

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @OnClick(R.id.layout_detail)
    void detail() {
        mLayoutSelect.setVisibility(View.VISIBLE);
        mImageMore.setImageResource(R.drawable.detail_btn_close);
    }

    @OnClick(R.id.imageView5)
    void close() {
        mLayoutSelect.setVisibility(View.INVISIBLE);
        mImageMore.setImageResource(R.drawable.detail_btn_open);
    }

    @OnClick(R.id.layout6)
    void select1() {
        mImage1.setImageResource(R.drawable.part_01_select);
        mImage2.setImageResource(R.drawable.part_02);
        mImage3.setImageResource(R.drawable.part_03);
        mImage4.setImageResource(R.drawable.part_04);
        mGroup1 = "포유류";
    }

    @OnClick(R.id.layout7)
    void select2() {
        mImage1.setImageResource(R.drawable.part_01);
        mImage2.setImageResource(R.drawable.part_02_select);
        mImage3.setImageResource(R.drawable.part_03);
        mImage4.setImageResource(R.drawable.part_04);
        mGroup1 = "양서류";
    }

    @OnClick(R.id.layout8)
    void select3() {
        mImage1.setImageResource(R.drawable.part_01);
        mImage2.setImageResource(R.drawable.part_02);
        mImage3.setImageResource(R.drawable.part_03_select);
        mImage4.setImageResource(R.drawable.part_04);
        mGroup1 = "조류";
    }

    @OnClick(R.id.layout9)
    void select4() {
        mImage1.setImageResource(R.drawable.part_01);
        mImage2.setImageResource(R.drawable.part_02);
        mImage3.setImageResource(R.drawable.part_03);
        mImage4.setImageResource(R.drawable.part_04_select);
        mGroup1 = "파충류";
    }

    @OnClick(R.id.button_report)
    void report() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file", mFile);

            UploadImage uploadImage = new UploadImage();
            uploadImage.execute(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class UploadImage extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                HttpProxy.getInstance().httpPostFile(URL_FILE, null, params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                JSONObject params = new JSONObject();
                params.put("group1", mGroup1);
                params.put("group2", "test");
                params.put("writeDate", mTimeReport);
                params.put("lng", mLng);
                params.put("lat", mLat);
                params.put("file", mFileName);

                PostReport postReport = new PostReport();
                postReport.execute(params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PostReport extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                Log.d(TAG, "length : " + params.length);
                HttpProxy.getInstance().httpPost(URL, null, params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            Toast.makeText(getContext(), "신고 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), CompleteActivity.class);
            intent.putExtra("time", mTimeString);
            intent.putExtra("location", mLocationString);
            intent.putExtra("group", mGroup1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }


    public void getBitmap(String path) {
        try {
            Bitmap bitmap = null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
              mImageView.setImageBitmap(bitmap);
//            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void getTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm");
        SimpleDateFormat reportDateFormat = new SimpleDateFormat("yyyyMMdd");
        mTimeString = simpleDateFormat.format(date);
        mTimeReport = reportDateFormat.format(date);
        mTime = date.getTime();
    }

    private void getLocation() {
        mLocationManager
                = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        Location location = null;

        if (!isGPSEnabled) {
            Toast.makeText(getContext(), "위치 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
        } else if (isGPSEnabled) {
            if (location == null) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }

                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (mLocationManager != null) {
                    location = mLocationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        mLat = location.getLatitude();
                        mLng = location.getLongitude();
                        mLocationString = getAddress(mLat, mLng);
                    }
                }
            }
            Log.d(TAG, "isGPSEnabled : " + isGPSEnabled + ", " + mLng + ", " + mLat);
        }
    }

    private String getAddress(double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(getContext(), Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddress;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getLocation();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng latLng = new LatLng(mLat, mLng);
        Marker seoul = googleMap.addMarker(new MarkerOptions().position(latLng));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
}
