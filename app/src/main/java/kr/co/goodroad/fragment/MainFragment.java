package kr.co.goodroad.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import kr.co.goodroad.R;
import kr.co.goodroad.activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = MainFragment.class.getName();
    private ImageButton mBtnCamera;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Uri mImagePath = null;
    private View mView;

    private static MainFragment mInstance;


    public static MainFragment getInstance() {
        if(mInstance == null) {
            mInstance = new MainFragment();
        }
        return mInstance;
    }

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setToolbarTitle(R.string.app_main_activity_title);

        mView = inflater.inflate(R.layout.fragment_main, container, false);
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG,"[MainFragment] keyCode(" + keyCode+ ") event(" + event.getAction() + ")");
                if (keyCode == KeyEvent.KEYCODE_BACK ) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        android.support.v7.app.AlertDialog.Builder alt_bld = new android.support.v7.app.AlertDialog.Builder(getContext());
                        alt_bld.setMessage(getString(R.string.exit_activity)).setCancelable(
                                false).setPositiveButton(getString(R.string.common_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        getActivity().finish();
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
                    }
                    return true;
                }
                return false;
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorTransparent));

        mBtnCamera = (ImageButton) getView().findViewById(R.id.imageButton_camera);
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "저장권한이 없어 신고 기능을 이용하실 수 없습니다.\n안드로이드 설정에서 굿로드에 저장 권한을 주신 후 이용 가능합니다.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                    return;
                }
                moveCamera();
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);

        if( requestCode == 1 ) {
            if( resultCode != RESULT_OK ) {
                return;
            }
            final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

            ft.replace(R.id.layout_content, ReportFragment.getInstance(getContext(), mImagePath, getContext().getContentResolver()));
            ft.commit();
            ft.addToBackStack(null);
        }
    }

    private Uri createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String path = File.separator + "GoodRoad";

        Log.d(TAG,"DIRECTORY : " + Environment.DIRECTORY_PICTURES);

        File goodroadDir = new File(storageDir.getAbsolutePath() + path);
        if( !goodroadDir.exists() ) {
            if( goodroadDir.mkdirs() == false ) {
                storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES);
                goodroadDir = new File(storageDir.getAbsolutePath() + path);
                if( !goodroadDir.exists() ) {
                    if( goodroadDir.mkdirs() == false ) {
                        storageDir = new File(Environment.getDataDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_PICTURES);
                        goodroadDir = new File(storageDir.getAbsolutePath() + path);
                        goodroadDir.mkdirs();
                    }
                }
            }
        }



        Log.d(TAG,"Absolute path : " + storageDir.getAbsolutePath() + path + "/" + imageFileName);

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getActivity(), "kr.co.goodroad.fileprovider", new File(storageDir.getAbsolutePath() + path + "/" + imageFileName));
        } else {
            uri = Uri.fromFile(new File(storageDir.getAbsolutePath() + path + "/" + imageFileName));
        }

        return uri;
    }

    void moveCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImagePath = createImageFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImagePath);
        startActivityForResult(intent, 1);
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
//
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
