package net.softminds.goodroad.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.softminds.goodroad.R;
import net.softminds.goodroad.activity.MainActivity;
import net.softminds.goodroad.adapter.PhoneNumberListAdapter;
import net.softminds.goodroad.model.PhoneNumberItem;

/**
 * Created by hjlee on 2017-08-11.
 */

public class Phone2Fragment extends Fragment {
    final static private String TAG = Phone2Fragment.class.getName();
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
                    String phoneNumber = item.getPhoneNumber();
                    if( position == 2 ) {
                        if(MainActivity.localNumber == null) return;
                        phoneNumber = MainActivity.localNumber + "128";
                    } else if( phoneNumber.contains("~")) {
                        phoneNumber = phoneNumber.substring(0,phoneNumber.indexOf("~"));
                    }
                    Log.d(TAG,"phoneNumber : " + phoneNumber);
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber));
                    startActivity(intent);
                }
            });
        }
    }
}
