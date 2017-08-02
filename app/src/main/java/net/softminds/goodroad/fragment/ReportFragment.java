package net.softminds.goodroad.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.daum.android.map.MapView;
import net.daum.mf.map.api.MapLayout;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.softminds.goodroad.R;
import net.softminds.goodroad.activity.MainActivity;
import net.softminds.goodroad.common.Definitions;
import net.softminds.goodroad.util.SMHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Date;
import java.text.SimpleDateFormat;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragment extends Fragment implements net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener, net.daum.mf.map.api.MapView.MapViewEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ReportFragment.class.getName();
    private String mFileName;

    private String mParam1;
    private String mParam2;
    MapPOIItem mCurrentMarker = null;

    private net.daum.mf.map.api.MapView mMapView;
    private ImageView mIvImageReport;

    private TextView mTvAddr;
    private TextView mTvTime;
    private Thread mThreadLocation;

    private View mView;

    private OnFragmentInteractionListener mListener;

    private static ReportFragment mInstance = null;

    Uri mImageDataUri;
    ContentResolver mContentResolver;
    Bitmap mBitmap = null;

    public static ReportFragment getInstance(Uri imageDataUri,ContentResolver contentResolver) {
        if (mInstance == null) {
            mInstance = new ReportFragment();
        }
        mInstance.mImageDataUri = imageDataUri;
        mInstance.mContentResolver = contentResolver;
        return mInstance;
    }

    public ReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportFragment.
     */
    public static ReportFragment newInstance(String param1, String param2) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void save(Uri uri, ContentResolver contentResolver) {
        //resize
        if( uri == null ) return;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            mBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);

            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                mIvImageReport = (ImageView) getView().findViewById(R.id.image_report);
                mIvImageReport.setImageBitmap(mBitmap);

                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "resize error : " + e.toString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setToolbarTitle(R.string.app_main_activity_title_for_report);
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_report, container, false);
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();

        final Fragment self = this;

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {
                    Log.i(TAG, "onKey Back listener is working!!!");
                    android.support.v7.app.AlertDialog.Builder alt_bld = new android.support.v7.app.AlertDialog.Builder(getContext());
                    alt_bld.setMessage(getString(R.string.cancel_report)).setCancelable(
                            false).setPositiveButton(getString(R.string.common_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    closeFragment();
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.common_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    android.support.v7.app.AlertDialog alert = alt_bld.create();
                    // Title for AlertDialog
                    alert.setTitle(getString(R.string.common_confirm));
                    alert.show();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return mView;
    }

    public void closeFragment() {
        if( mThreadLocation != null ) {
            try {
                mThreadLocation.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        save(mImageDataUri,mContentResolver);

        mTvAddr = (TextView) getView().findViewById(R.id.tv_addr);
        mTvTime = (TextView) getView().findViewById(R.id.tv_time);
        long time = System.currentTimeMillis();

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년M월d일 a hh:mm");

        String str = dayTime.format(new Date(time));

        mTvTime.setText(str);

        createMapView();
    }

    private void createMapView() {
        MapLayout mapLayout = new MapLayout((Activity) getView().getContext());
        mMapView = mapLayout.getMapView();


        mMapView.setDaumMapApiKey(Definitions.DAUM_MAPS_ANDROID_APP_API_KEY);
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setMapType(net.daum.mf.map.api.MapView.MapType.Standard);

        ViewGroup mapViewContainer = (ViewGroup) getView().findViewById(R.id.map_view);
        mapViewContainer.addView(mapLayout);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG,"onDestroyView");

        mBitmap.recycle();
        mBitmap = null;
        ((BitmapDrawable) mIvImageReport.getDrawable()).getBitmap().recycle();
    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(net.daum.mf.map.api.MapView mapView, int resultCode, String resultMessage) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // net.daum.mf.map.api.MapView.MapViewEventListener

    public void onMapViewInitialized(net.daum.mf.map.api.MapView mapView) {
        Log.i(TAG, "MapView had loaded. Now, MapView APIs could be called safely");
        //mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        if(MainActivity.mCurrentLocation != null ) {
            mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(MainActivity.mCurrentLocation.getLatitude(), MainActivity.mCurrentLocation.getLongitude()), 2, true);
        }
    }

    @Override
    public void onMapViewCenterPointMoved(net.daum.mf.map.api.MapView mapView, MapPoint mapCenterPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapCenterPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onMapViewCenterPointMoved (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
        setCenterMarker();
    }

    private void setCenterMarker() {

        if( mCurrentMarker != null ) {
            mMapView.removePOIItem(mCurrentMarker);
            mCurrentMarker = null;
        }
        mCurrentMarker = new MapPOIItem();
        MapPoint center = mMapView.getMapCenterPoint();

        mCurrentMarker.setItemName("현재 위치");
        mCurrentMarker.setTag(0);
        mCurrentMarker.setMapPoint(center);
        mCurrentMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        mCurrentMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mMapView.addPOIItem(mCurrentMarker);
    }

    @Override
    public void onMapViewDoubleTapped(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {


    }

    @Override
    public void onMapViewSingleTapped(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onMapViewSingleTapped (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
    }

    @Override
    public void onMapViewDragStarted(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onMapViewDragStarted (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
    }

    @Override
    public void onMapViewDragEnded(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onMapViewDragEnded (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
    }

    @Override
    public void onMapViewMoveFinished(net.daum.mf.map.api.MapView mapView, MapPoint mapPoint) {
        final MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onMapViewMoveFinished (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
        setCenterMarker();
        mThreadLocation = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject addr = SMHttpClient.execute("GET","https://apis.daum.net/local/geo/coord2addr",null,
                        "apikey=" + Definitions.DAUM_REST_API_KEY + "&longitude=" + mapPointGeo.longitude + "&latitude=" + mapPointGeo.latitude + "&inputCoordSystem=WGS84&output=json",null);

                try {
                    if( addr != null ) {
                        final String fullName = addr.getString("fullName");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG,"주소 : " + fullName);
                                mTvAddr.setText(fullName);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mThreadLocation = null;
                }
            }
        });
        mThreadLocation.start();
    }

    @Override
    public void onMapViewZoomLevelChanged(net.daum.mf.map.api.MapView mapView, int zoomLevel) {
        Log.i(TAG, String.format("MapView onMapViewZoomLevelChanged (%d)", zoomLevel));
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
