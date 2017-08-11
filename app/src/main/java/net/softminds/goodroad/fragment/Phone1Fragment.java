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
import net.softminds.goodroad.adapter.PhoneNumberListAdapter;
import net.softminds.goodroad.model.PhoneNumberItem;

/**
 * Created by hjlee on 2017-08-11.
 */

public class Phone1Fragment extends Fragment {
    final static private String TAG = Phone1Fragment.class.getName();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        int resId = R.layout.fragment_phone1;
        return inflater.inflate(resId, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if( view != null ) {
            ListView lvPhone1 = (ListView) view.findViewById(R.id.phone1_list_view);
            final PhoneNumberListAdapter adapter1 = new PhoneNumberListAdapter();
            lvPhone1.setAdapter(adapter1);

            adapter1.addItem("강원", "033-250-7504");
            adapter1.addItem("경기", "031-8008-6210");
            adapter1.addItem("경남", "055-754-9575");
            adapter1.addItem("경북", "054-840-8250");
            adapter1.addItem("부산", "051-209-2090~3");
            adapter1.addItem("울산", "052-256-5322~3");
            adapter1.addItem("전남", "061-749-3898");
            adapter1.addItem("전북", "063-850-0956");
            adapter1.addItem("제주", "064-752-9582");
            adapter1.addItem("충남", "041-330-1666");
            adapter1.addItem("충북", "043-216-3328");
            lvPhone1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    PhoneNumberItem item = (PhoneNumberItem) adapter1.getItem(position);
                    String phoneNumber = item.getPhoneNumber();
                    if( phoneNumber.contains("~")) {
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
