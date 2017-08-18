package net.softminds.goodroad.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapLayout;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.softminds.goodroad.R;
import net.softminds.goodroad.activity.MainActivity;
import net.softminds.goodroad.common.Definitions;
import net.softminds.goodroad.util.ImageUtil;
import net.softminds.goodroad.util.SMHttpClient;
import net.softminds.goodroad.util.UriUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import static net.softminds.goodroad.activity.MainActivity.mCurrentLocation;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragment extends Fragment implements net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener, net.daum.mf.map.api.MapView.MapViewEventListener, MainActivity.OnLocationChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ReportFragment.class.getName();
    private String mFileName;

    private String mParam1;
    private String mParam2;
    MapPOIItem mCurrentMarker = null;
    private File mFile;

    private net.daum.mf.map.api.MapView mMapView;
    private ImageView mIvImageReport;

    private TextView mTvAddr;
    private TextView mTvTime;
    private String mDate;
    private Thread mThreadLocation;

    private ImageView mIvOpenSelectGroupType;
    private LinearLayout mLinearLayoutSelectGroupType;
    private RelativeLayout mRelativeLayoutBtnOpenSelectGroupType;
    private LinearLayout mLinearLayoutSelectGroupTypePanel;

    private LinearLayout mLinearLayoutSending;
    private TextView mTvSendingInfo;
    private int mLocationUpdateReceived = 0;

    private RelativeLayout mLayoutReport;

    private RelativeLayout mRelativeLayoutGroupType1;
    private RelativeLayout mRelativeLayoutGroupType2;
    private RelativeLayout mRelativeLayoutGroupType3;
    private RelativeLayout mRelativeLayoutGroupType4;
    private RelativeLayout mRelativeLayoutGroupType1OnPop;
    private RelativeLayout mRelativeLayoutGroupType2OnPop;
    private RelativeLayout mRelativeLayoutGroupType3OnPop;
    private RelativeLayout mRelativeLayoutGroupType4OnPop;
    private RelativeLayout mRelativeLayoutSpeciesButtons;

    private RelativeLayout mRelativeLayoutSavedGroupSpecies;
    private TextView mTvSavedGroup;
    private TextView mTvSavedSpecies;

    private ImageView mIvGroup1;
    private ImageView mIvGroup2;
    private ImageView mIvGroup3;
    private ImageView mIvGroup4;
    private ImageView mIvGroup1OnPop;
    private ImageView mIvGroup2OnPop;
    private ImageView mIvGroup3OnPop;
    private ImageView mIvGroup4OnPop;
    private ImageView mIvCloseSelectGroupPanel;
    private View mView;
    private String mSelectedGroup = "";
    private Button mBtnSave = null;
    private Button mBtnReport = null;
    private EditText mEditTextSpecies;
    private ImageButton mIbClearSpecies;
    private String mSavedGroup = "";
    private String mSavedSpecies = "";
    private OnFragmentInteractionListener mListener;

    private static final String URL = "http://rest.goodroad.co.kr/api/reports";
    private static final String URL_FILE = "http://rest.goodroad.co.kr/upload";

    private static ReportFragment mInstance = null;

    private Uri mImageDataUri;
    private ContentResolver mContentResolver;
    private Bitmap mBitmap = null;

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);  // your capture image URL code
        return Uri.parse(path);

    }

    public static ReportFragment getInstance(Context context, Uri imageDataUri, ContentResolver contentResolver) {

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
            ExifInterface exif = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    exif = new ExifInterface(contentResolver.openInputStream(uri));
                } else {
                    exif = new ExifInterface(UriUtil.getRealPathFromURI(uri,contentResolver));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);

            mBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);

            if( mBitmap != bitmap ) {
                bitmap.recycle();
            }
            mBitmap = ImageUtil.rotateBitmap(mBitmap, orientation);


        } catch (IOException e) {
            e.printStackTrace();
        }

        //save
        String path = Environment.getExternalStorageDirectory().getPath() + "/GoodRoad/";
        File pathFile = new File(path);
        if(!pathFile.isDirectory()) {
            pathFile.mkdirs();
        }

        pathFile = new File(path);
        if(!pathFile.isDirectory()) {
            path = "/sdcard/GoodRoad/";
            File dir = new File(path);
            dir.mkdirs();
        }

        mFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

        mFile = new File(new File(path), mFileName);
        if (mFile.exists()) {
            mFile.delete();
        }

        Log.d(TAG,"File path : " + path + mFileName);
        if(!mFile.exists()) {
            try {
                mFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(mFile);
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
        MainActivity.setLocationChangeListner(this);
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_report, container, false);
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();

        final Fragment self = this;

        String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if( !provider.contains("gps") ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(self.getActivity(), "위치 기능이 꺼져있습니다.\n지도에서 드래그하여\n현재 위치를 선택해주세요.", Toast.LENGTH_LONG).show();
                }
            });
        }



        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    }

                    if( mLinearLayoutSending.getVisibility() == View.VISIBLE ) return true;

                    if( mLinearLayoutSelectGroupTypePanel.getVisibility() == View.VISIBLE ) {
                        closeSelectGroupPanel();

                        return true;
                    }

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

    private void closeSelectGroupPanel() {
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                R.anim.bottom_down);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLinearLayoutSelectGroupTypePanel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mLinearLayoutSelectGroupTypePanel.startAnimation(bottomDown);


//        Animation topDown = AnimationUtils.loadAnimation(getContext(),
//                R.anim.top_down);
//        mLayoutReport.startAnimation(topDown);
//        mLayoutReport.setVisibility(View.VISIBLE);
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
        mSelectedGroup = "";
        mLayoutReport = (RelativeLayout) getView().findViewById(R.id.layout_report);

        mSavedSpecies = "";
        mSavedGroup = "";
        save(mImageDataUri,mContentResolver);

        mTvAddr = (TextView) getView().findViewById(R.id.tv_addr);
        mTvTime = (TextView) getView().findViewById(R.id.tv_time);
        mIvOpenSelectGroupType = (ImageView) getView().findViewById(R.id.iv_btn_open_select_group_type);
        mLinearLayoutSelectGroupType = (LinearLayout) getView().findViewById(R.id.layout_select_group_type);

        mRelativeLayoutBtnOpenSelectGroupType = (RelativeLayout) getView().findViewById(R.id.layout_btn_open_select_group_type);
        mRelativeLayoutBtnOpenSelectGroupType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mLinearLayoutSelectGroupType.getVisibility() == View.VISIBLE ) {
                    mLinearLayoutSelectGroupType.setVisibility(View.GONE);
                    mIvOpenSelectGroupType.setImageDrawable(getResources().getDrawable(R.mipmap.btn_open_detail));
                } else {
                    mLinearLayoutSelectGroupType.setVisibility(View.VISIBLE);
                    mIvOpenSelectGroupType.setImageDrawable(getResources().getDrawable(R.mipmap.btn_close_detail));
                }
            }
        });

        mTvSendingInfo = (TextView) getActivity().findViewById(R.id.tv_sending_info);
        mEditTextSpecies = (EditText) getView().findViewById(R.id.et_species);
        mIbClearSpecies = (ImageButton) getView().findViewById(R.id.ib_clear_species);

        mEditTextSpecies.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {

                    if( mLinearLayoutSending.getVisibility() == View.VISIBLE ) return true;

                    if (mLinearLayoutSelectGroupTypePanel.getVisibility() == View.VISIBLE) {
                        closeSelectGroupPanel();
                        return true;
                    }
                }

                invalidateButtons();
                return false;
            }
        });

        mLinearLayoutSending = (LinearLayout) getActivity().findViewById(R.id.layout_sending);

        mRelativeLayoutSavedGroupSpecies = (RelativeLayout) getView().findViewById(R.id.layout_saved_group_species);
        mTvSavedGroup = (TextView) getView().findViewById(R.id.tv_saved_group);
        mTvSavedSpecies = (TextView) getView().findViewById(R.id.tv_saved_species);



        mEditTextSpecies.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                invalidateButtons();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                invalidateButtons();
            }
        });

        mIbClearSpecies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditTextSpecies.setText("");
                invalidateButtons();
            }
        });

        mBtnSave = (Button) getView().findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSavedSpecies = mEditTextSpecies.getText().toString();
                mSavedGroup = mSelectedGroup;
                closeSelectGroupPanel();
                mRelativeLayoutSavedGroupSpecies.setVisibility(View.VISIBLE);
                mTvSavedGroup.setText(mSavedGroup);
                mTvSavedSpecies.setText(mSavedSpecies);
                mLinearLayoutSelectGroupType.setVisibility(View.GONE);
                mIvOpenSelectGroupType.setImageDrawable(getResources().getDrawable(R.mipmap.btn_open_detail));
            }
        });

        mBtnReport = (Button) getView().findViewById(R.id.btn_report);
        mBtnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    report();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mRelativeLayoutSpeciesButtons = (RelativeLayout) getView().findViewById(R.id.layout_species_buttons);

        mRelativeLayoutBtnOpenSelectGroupType = (RelativeLayout) getView().findViewById(R.id.layout_btn_open_select_group_type);
        mLinearLayoutSelectGroupTypePanel = (LinearLayout) getView().findViewById(R.id.layout_select_group_and_species_type_view);

        mRelativeLayoutGroupType1 = (RelativeLayout) getView().findViewById(R.id.layout_group_type_1);
        mRelativeLayoutGroupType2 = (RelativeLayout) getView().findViewById(R.id.layout_group_type_2);
        mRelativeLayoutGroupType3 = (RelativeLayout) getView().findViewById(R.id.layout_group_type_3);
        mRelativeLayoutGroupType4 = (RelativeLayout) getView().findViewById(R.id.layout_group_type_4);

        mRelativeLayoutGroupType1.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group1)));
        mRelativeLayoutGroupType2.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group2)));
        mRelativeLayoutGroupType3.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group3)));
        mRelativeLayoutGroupType4.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group4)));

        mRelativeLayoutGroupType1OnPop = (RelativeLayout) getView().findViewById(R.id.layout_group_type_1_on_pop);
        mRelativeLayoutGroupType2OnPop = (RelativeLayout) getView().findViewById(R.id.layout_group_type_2_on_pop);
        mRelativeLayoutGroupType3OnPop = (RelativeLayout) getView().findViewById(R.id.layout_group_type_3_on_pop);
        mRelativeLayoutGroupType4OnPop = (RelativeLayout) getView().findViewById(R.id.layout_group_type_4_on_pop);

        mRelativeLayoutGroupType1OnPop.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group1)));
        mRelativeLayoutGroupType2OnPop.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group2)));
        mRelativeLayoutGroupType3OnPop.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group3)));
        mRelativeLayoutGroupType4OnPop.setOnClickListener(new OnSelectGroupClickListener(getString(R.string.report_group4)));

        mIvGroup1 = (ImageView) getView().findViewById(R.id.iv_group_type_01);
        mIvGroup2 = (ImageView) getView().findViewById(R.id.iv_group_type_02);
        mIvGroup3 = (ImageView) getView().findViewById(R.id.iv_group_type_03);
        mIvGroup4 = (ImageView) getView().findViewById(R.id.iv_group_type_04);
        mIvGroup1OnPop = (ImageView) getView().findViewById(R.id.iv_group_type_01_on_pop);
        mIvGroup2OnPop = (ImageView) getView().findViewById(R.id.iv_group_type_02_on_pop);
        mIvGroup3OnPop = (ImageView) getView().findViewById(R.id.iv_group_type_03_on_pop);
        mIvGroup4OnPop = (ImageView) getView().findViewById(R.id.iv_group_type_04_on_pop);

        mIvCloseSelectGroupPanel = (ImageView) getView().findViewById(R.id.iv_btn_close_select_group_type);

        mIvCloseSelectGroupPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSelectGroupPanel();
            }
        });

        long time = System.currentTimeMillis();

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년M월d일 a hh:mm");

        String str = dayTime.format(new Date(time));

        mTvTime.setText(str);

        SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
        mDate = day.format(new java.util.Date(time));

        createMapView();
    }

    private void invalidateButtons() {
        if( isDetached() || getHost() == null ) return;
        if( mEditTextSpecies.getText() == null || mEditTextSpecies.getText().toString().isEmpty()) {
            mBtnSave.setEnabled(false);
            mBtnSave.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
            mBtnSave.setTextColor(getResources().getColor(R.color.colorGray));
        } else {
            mBtnSave.setEnabled(true);
            mBtnSave.setBackgroundColor(getResources().getColor(R.color.colorDarkGray));
            mBtnSave.setTextColor(getResources().getColor(R.color.colorYellow));
        }
        for( int i = 0 ; i < mRelativeLayoutSpeciesButtons.getChildCount() ; i++ ) {
            Button btn = (Button)mRelativeLayoutSpeciesButtons.getChildAt(i);
            GradientDrawable gd = new GradientDrawable();
            if( mEditTextSpecies.getText() == null || mEditTextSpecies.getText().toString().isEmpty()
                    || (!mEditTextSpecies.getText().toString().equals(btn.getText().toString())) ) {
                gd.setColor(getResources().getColor(R.color.colorYellow));
                gd.setStroke(ImageUtil.dpToPx(1,getContext()), getResources().getColor(R.color.colorDarkYellow));
            } else {
                gd.setColor(getResources().getColor(R.color.colorDarkYellow));
                gd.setStroke(ImageUtil.dpToPx(3,getContext()), getResources().getColor(R.color.colorStrongYellow));
            }
            gd.setCornerRadius(ImageUtil.dpToPx(15,getContext()));
            btn.setBackgroundDrawable(gd);
        }
    }

    private void createMapView() {

        if( !Build.FINGERPRINT.contains("generic") ) {
            MapLayout mapLayout = new MapLayout((Activity) getView().getContext());
            mMapView = mapLayout.getMapView();


            mMapView.setDaumMapApiKey(Definitions.DAUM_MAPS_ANDROID_APP_API_KEY);
            mMapView.setOpenAPIKeyAuthenticationResultListener(this);
            mMapView.setMapViewEventListener(this);
            mMapView.setMapType(net.daum.mf.map.api.MapView.MapType.Standard);

            ViewGroup mapViewContainer = (ViewGroup) getView().findViewById(R.id.map_view);
            mapViewContainer.addView(mapLayout);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        if( mBitmap != null ) {
            mBitmap.recycle();
            mBitmap = null;
            ((BitmapDrawable) mIvImageReport.getDrawable()).getBitmap().recycle();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if( mBitmap != null ) {
            mBitmap.recycle();
            mBitmap = null;
            ((BitmapDrawable) mIvImageReport.getDrawable()).getBitmap().recycle();
        }
        Log.d(TAG,"onDestroyView");
        MainActivity.setLocationChangeListner(null);
    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(net.daum.mf.map.api.MapView mapView, int resultCode, String resultMessage) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // net.daum.mf.map.api.MapView.MapViewEventListener

    public void onMapViewInitialized(net.daum.mf.map.api.MapView mapView) {
        Log.i(TAG, "[LOCATION] MapView had loaded. Now, MapView APIs could be called safely");
        //mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        if(mCurrentLocation != null ) {
            Log.d(TAG,"[LOCATION] mCurrentLocation is not null. Centering");
            mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 2, true);
            mLocationUpdateReceived++;
        } else {
            Log.d(TAG,"[LOCATION] mCurrentLocation is null.");
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

                try {
                    JSONObject addr = SMHttpClient.execute("GET","https://apis.daum.net/local/geo/coord2addr",null,
                            "apikey=" + Definitions.DAUM_REST_API_KEY + "&longitude=" + mapPointGeo.longitude + "&latitude=" + mapPointGeo.latitude + "&inputCoordSystem=WGS84&output=json",null);
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
                } catch (Exception e) {
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

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"[LOCATION] onLocationChanged(Fragment)");
        Log.d(TAG,"[LOCATION] mLocationUpdateReceived(" + mLocationUpdateReceived + ") mCurrentLocation(" + mCurrentLocation + ")");
        if( mLocationUpdateReceived == 0 && mCurrentLocation != null ) {

            if( mMapView != null ) {
                Log.d(TAG,"[LOCATION] map view is not null. Centering");
                mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 2, true);
            }else {
                Log.d(TAG,"[LOCATION] map view is null.");
            }
            mLocationUpdateReceived++;
        }
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

    private class OnSelectGroupClickListener implements View.OnClickListener {
        private String mGroupName;
        public OnSelectGroupClickListener(String groupName) {
            mGroupName = groupName;
        }

        @Override
        public void onClick(View view) {

            selectGroup(mGroupName);
            Log.d(TAG,"SELECTED GROUP:" + mGroupName);
            if( mLinearLayoutSelectGroupTypePanel.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                        R.anim.bottom_up);
                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mLinearLayoutSelectGroupTypePanel.startAnimation(bottomUp);

//                Animation topUp = AnimationUtils.loadAnimation(getContext(),
//                        R.anim.top_up);
//                topUp.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        mLayoutReport.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                mLayoutReport.startAnimation(topUp);

                invalidateButtons();
                mLinearLayoutSelectGroupTypePanel.setVisibility(View.VISIBLE);
                if( mSelectedGroup.equals(mSavedGroup) ) {
                    mEditTextSpecies.setText(mSavedSpecies);
                } else {
                    mEditTextSpecies.setText("");
                }

            }
        }
    }

    private void selectGroup(String groupName) {

        if( mSelectedGroup != null && mSelectedGroup.equals(groupName) ) {
            Log.d(TAG,"SELECTED GROUP(not changed):" + groupName);
            return;
        }

        mEditTextSpecies.setText("");

        mSelectedGroup = groupName;
        if( mSelectedGroup.equals(getString(R.string.report_group1))) {
            mIvGroup1.setImageDrawable(getResources().getDrawable(R.mipmap.group_01_select));
            mIvGroup1OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_01_select));
            mIvGroup2.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup2OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup3.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup3OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup4.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            mIvGroup4OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            setSpeciesButtons(Definitions.groupSpecies1);
        } else if( mSelectedGroup.equals(getString(R.string.report_group2))) {
            mIvGroup1.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup1OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup2.setImageDrawable(getResources().getDrawable(R.mipmap.group_02_select));
            mIvGroup2OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_02_select));
            mIvGroup3.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup3OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup4.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            mIvGroup4OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            setSpeciesButtons(Definitions.groupSpecies2);
        } else if( mSelectedGroup.equals(getString(R.string.report_group3))) {
            mIvGroup1.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup1OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup2.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup2OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup3.setImageDrawable(getResources().getDrawable(R.mipmap.group_03_select));
            mIvGroup3OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_03_select));
            mIvGroup4.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            mIvGroup4OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_04));
            setSpeciesButtons(Definitions.groupSpecies3);
        } else if( mSelectedGroup.equals(getString(R.string.report_group4))) {
            mIvGroup1.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup1OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_01));
            mIvGroup2.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup2OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_02));
            mIvGroup3.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup3OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_03));
            mIvGroup4.setImageDrawable(getResources().getDrawable(R.mipmap.group_04_select));
            mIvGroup4OnPop.setImageDrawable(getResources().getDrawable(R.mipmap.group_04_select));
            setSpeciesButtons(Definitions.groupSpecies4);
        }
    }

    private void setSpeciesButtons(String[] groupSpecies) {
        mRelativeLayoutSpeciesButtons.removeAllViews();
        Button oldButton = null;
        Button lineFirstButton = null;
        Button lastLineFirstButton = null;
        int index = 0;
        int countPerRow = 3;
        for( String species : groupSpecies ) {
            final Button newButton = new Button(this.getContext());
            newButton.setText(species);
            newButton.setId(index + 20000);
            newButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEditTextSpecies.setText(newButton.getText().toString());
                    invalidateButtons();
                }
            });

            newButton.setTextColor(getResources().getColor(R.color.colorWhite));

            int left = 20;
            int right = 0;
            int top = 12;
            int bottom = 0;

            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            if(index < countPerRow ) {
                top = 0;
            }

            if( lastLineFirstButton != null ) {
                p.addRule(RelativeLayout.BELOW, lastLineFirstButton.getId());
            }

            if( ( lineFirstButton != null ) && ( (index % countPerRow) == 0 ) ) {
                p.addRule(RelativeLayout.BELOW, lineFirstButton.getId());
            } else if( oldButton != null ) {
                p.addRule(RelativeLayout.RIGHT_OF, oldButton.getId());
            }

            p.setMargins(
                    ImageUtil.dpToPx(left,getContext()),
                    ImageUtil.dpToPx(top,getContext()),
                    ImageUtil.dpToPx(right,getContext()),
                    ImageUtil.dpToPx(bottom,getContext()));
            newButton.setLayoutParams(p);
            newButton.setMinWidth(0);
            newButton.setMinHeight(0);
            newButton.setMinimumHeight(0);
            newButton.setMinimumWidth(0);
            newButton.setHeight(ImageUtil.dpToPx(40,getContext()));
            newButton.setPadding(
                    ImageUtil.dpToPx(20,getContext()),
                    ImageUtil.dpToPx(3,getContext()),
                    ImageUtil.dpToPx(20,getContext()),
                    ImageUtil.dpToPx(3,getContext())
            );

            mRelativeLayoutSpeciesButtons.addView(newButton);

            if( ( index % countPerRow ) == 0 ) {
                lastLineFirstButton = lineFirstButton;
                lineFirstButton = newButton;

            }
            oldButton = newButton;
            index++;
        }
        invalidateButtons();
    }

    private void openCompleteFragment() {

        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        String groupType = "";
        if( mSavedGroup == null || mSavedGroup.isEmpty() ) {
            groupType = "설정하지 않음";
        } else {
            groupType = mSavedGroup + ", " + mSavedSpecies;
        }
        ft.replace(R.id.layout_content, ReportCompleteFragment.newInstance(mTvTime.getText().toString(),mTvAddr.getText().toString(),groupType));
        getActivity().getSupportFragmentManager().popBackStack();
        ft.commit();
        ft.addToBackStack(null);

    }



    Exception mReportException = null;
    private class UploadImage extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvSendingInfo.setText(SENDING_STEP2);
                    }
                });
                mReportException = null;
                SMHttpClient.postFile(URL_FILE, null, params[0]);
            } catch (Exception e) {
                mReportException = e;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                if( mReportException != null ) {
                    Toast.makeText(getContext(), "오류가 발생하였습니다.\n" + mReportException.getMessage(), Toast.LENGTH_LONG).show();
                    hideSendingScreen();
                    return;
                }
                JSONObject params = new JSONObject();
                params.put("group1", mSavedGroup);
                params.put("group2", mSavedSpecies);
                params.put("writeDate", mDate);
                params.put("lng", mMapView.getMapCenterPoint().getMapPointGeoCoord().longitude);
                params.put("lat", mMapView.getMapCenterPoint().getMapPointGeoCoord().latitude);
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
            getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTvSendingInfo.setText(SENDING_STEP3);
                                            }
                                        });

            mReportException = null;
            try {
                SMHttpClient.execute("POST",URL,null,null,params[0].toString());
            } catch (Exception e) {
                mReportException = e;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if( mReportException != null ) {
                Toast.makeText(getContext(), "오류가 발생하였습니다.\n" + mReportException.getMessage(), Toast.LENGTH_LONG).show();
                mLinearLayoutSending.setVisibility(View.GONE);
                return;
            }

            Toast.makeText(getContext(), "신고 되었습니다.", Toast.LENGTH_SHORT).show();
            openCompleteFragment();
        }
    }


    private String SENDING_STEP1;
    private String SENDING_STEP2;
    private String SENDING_STEP3;

    private void report() throws JSONException {

        if( mTvAddr.getText() == null || mTvAddr.getText().toString().trim().isEmpty() ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "지도에서 현재 위치를 지정해주세요.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        SENDING_STEP1 = getString(R.string.sending_step_1);
        SENDING_STEP2 = getString(R.string.sending_step_2);
        SENDING_STEP3 = getString(R.string.sending_step_3);
        showSendingScreen();
        mTvSendingInfo.setText(SENDING_STEP1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("file", mFile);

        UploadImage uploadImage = new UploadImage();
        uploadImage.execute(jsonObject);
    }

    private void showSendingScreen() {
        mLinearLayoutSending.setVisibility(View.VISIBLE);
        mBtnReport.setEnabled(false);
    }

    private void hideSendingScreen() {
        mLinearLayoutSending.setVisibility(View.GONE);
        mBtnReport.setEnabled(true);
    }
}
