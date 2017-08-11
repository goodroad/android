package net.softminds.goodroad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

import net.softminds.goodroad.R;
import net.softminds.goodroad.model.PhoneNumberItem;

/**
 * Created by hjlee on 2017-08-11.
 */

public class PhoneNumberListAdapter extends BaseAdapter {

    List<PhoneNumberItem> mPhoneNumbers;

    public PhoneNumberListAdapter() {
        mPhoneNumbers = new ArrayList<PhoneNumberItem>();
    }

    @Override
    public int getCount() {
        return mPhoneNumbers.size();
    }

    @Override
    public Object getItem(int i) {
        return mPhoneNumbers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mPhoneNumbers.get(i).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_phone_number, parent, false);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name) ;
        TextView tvPhoneNumber = (TextView) convertView.findViewById(R.id.tv_phone_number) ;


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        PhoneNumberItem listViewItem = mPhoneNumbers.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        tvName.setText(listViewItem.getName());
        tvPhoneNumber.setText(listViewItem.getPhoneNumber());

        return convertView;
    }

    public void addItem(String name, String phoneNumber) {
        PhoneNumberItem item = new PhoneNumberItem();
        item.setId(mPhoneNumbers.size() + 1);
        item.setName(name);
        item.setPhoneNumber(phoneNumber);
        mPhoneNumbers.add(item);
    }
}
