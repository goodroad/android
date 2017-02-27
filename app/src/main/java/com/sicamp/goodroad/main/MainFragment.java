package com.sicamp.goodroad.main;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sicamp.goodroad.HttpProxy;
import com.sicamp.goodroad.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.Settings.Global.AIRPLANE_MODE_ON;

public class MainFragment extends Fragment implements LocationListener, View.OnClickListener {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String URL = "http://rest.goodroad.co.kr/api/reports";
    private static final String URL_FILE = "http://rest.goodroad.co.kr/upload";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private long mTime;
    private String mTimeString;
    private double mLng;
    private double mLat;
    private String mLocationString;
    private String mGroup1;
    private String mFileName;
    private File mFile;

    private Location mLocation;
    private LocationManager mLocationManager;

    private TextView mTextTime;
    private TextView mTextLocation;
    private Button mBtnReport;
    private Button mBtnPhoto;
    private ImageView mImageView;

    private CheckBox mBtn1, mBtn2, mBtn3, mBtn4;

    public static MainFragment getInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mTextTime = (TextView) view.findViewById(R.id.textView_time);
        mTextLocation = (TextView) view.findViewById(R.id.textView_location);
        mBtnReport = (Button) view.findViewById(R.id.btn_report);
        mBtnPhoto = (Button) view.findViewById(R.id.btn_photo);
        mBtn1 = (CheckBox) view.findViewById(R.id.checkBox1);
        mBtn2 = (CheckBox) view.findViewById(R.id.checkBox2);
        mBtn3 = (CheckBox) view.findViewById(R.id.checkBox3);
        mBtn4 = (CheckBox) view.findViewById(R.id.checkBox4);
        mImageView = (ImageView) view.findViewById(R.id.photo);

        btnGroup();

        mBtnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("file", mFile);

                UploadImage uploadImage = new UploadImage();
                uploadImage.execute(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }
        });

        mBtnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });

        getTime();
        getLocation();

        mTextTime.setText(mTimeString);
        mTextLocation.setText(mLocationString);

        isAirplaneModeOn(getContext());
        checkpermission();

        String path = Environment.getExternalStorageDirectory().toString() + "/GoodRoad/";
        File file = new File(path);
        if(!file.isDirectory()) {
            file.mkdirs();
        }

        return view;
    }

    private void getTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        mTimeString = simpleDateFormat.format(date);
        mTime = date.getTime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            getLocation();
            mTextLocation.setText(mLocationString);
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkBox1:
                mBtn2.setChecked(false);
                mBtn3.setChecked(false);
                mBtn4.setChecked(false);
                mGroup1 = getContext().getString(R.string.btn_group_1);
                break;
            case R.id.checkBox2:
                mBtn1.setChecked(false);
                mBtn3.setChecked(false);
                mBtn4.setChecked(false);
                mGroup1 = getContext().getString(R.string.btn_group_2);
                break;
            case R.id.checkBox3:
                mBtn1.setChecked(false);
                mBtn2.setChecked(false);
                mBtn4.setChecked(false);
                mGroup1 = getContext().getString(R.string.btn_group_3);
                break;
            case R.id.checkBox4:
                mBtn1.setChecked(false);
                mBtn2.setChecked(false);
                mBtn3.setChecked(false);
                mGroup1 = getContext().getString(R.string.btn_group_4);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Picasso.with(getContext()).load(data.getData()).into(mImageView);

        resize(data.getData());
    }

    static boolean isAirplaneModeOn(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
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
                mFile = file;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "resize error : " + e.toString());
            }
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
                params.put("writeDate", String.valueOf(mTime));
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
        }
    }

    private void btnGroup() {
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mBtn4.setOnClickListener(this);
    }

    @Override
    public void onProviderDisabled(String provider) {

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

    private void checkpermission() {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    public Uri getUriFromPath(String path) {
//        Uri fileUri = Uri.parse(path);
//        String filePath = fileUri.getPath();
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + path + "'", null, null);
        Log.d(TAG, "path 3 : " + cursor.getCount());
        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        Log.d(TAG, "path 2 : " + path);

        return uri;
    }

    private String getPathFromUri(Uri uri){
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        cursor.close();
        Log.d(TAG, "path 1 : " + path + ", " + uri);
        return path;
    }
}
