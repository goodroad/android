package kr.co.goodroad.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import kr.co.goodroad.R;
import kr.co.goodroad.activity.MainActivity;
import kr.co.goodroad.adapter.PhoneNumberListAdapter;
import kr.co.goodroad.model.PhoneNumberItem;

/**
 * Created by hjlee on 2017-08-11.
 */

public class Phone2Fragment extends Fragment {
    final static private String TAG = Phone2Fragment.class.getName();

    String mPhoneNumber;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        int resId = R.layout.fragment_phone2;
        return inflater.inflate(resId, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if( view != null ) {
            ListView lvPhone2 = (ListView) view.findViewById(R.id.phone2_list_view);
            final PhoneNumberListAdapter adapter2 = new PhoneNumberListAdapter();
            lvPhone2.setAdapter(adapter2);

            adapter2.addItem("고속도로(한국도로공사)", "1588-2504");
            adapter2.addItem("일반국도/서울", "120");
            adapter2.addItem("일반국도/지방", "(지역번호)+128");

            lvPhone2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                    PhoneNumberItem item = (PhoneNumberItem) adapter2.getItem(position);
                    mPhoneNumber = item.getPhoneNumber();
                    if( position == 2 ) {
                        if(MainActivity.localNumber == null) return;
                        mPhoneNumber = MainActivity.localNumber + "128";
                    } else if( mPhoneNumber.contains("~")) {
                        mPhoneNumber = mPhoneNumber.substring(0,mPhoneNumber.indexOf("~"));
                    }
                    Log.d(TAG,"phoneNumber : " + mPhoneNumber);
                    int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);

                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                getActivity(),
                                new String[]{Manifest.permission.CALL_PHONE},
                                1);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                    startActivity(intent);
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;
            default:
                break;
        }
    }
}
