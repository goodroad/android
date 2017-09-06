package kr.co.goodroad.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.goodroad.R;
import kr.co.goodroad.model.ExpandableMenuListItem;

import java.util.List;

/**
 * Created by hjlee on 2017-07-31.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<ExpandableMenuListItem> mExpandableListMenu;
    private LayoutInflater mLayoutInflater;

    public CustomExpandableListAdapter(Context context, List<ExpandableMenuListItem> expandableListMenu) {
        mContext = context;
        mExpandableListMenu = expandableListMenu;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ExpandableMenuListItem getChild(int listPosition, int expandedListPosition) {
        return mExpandableListMenu.get(listPosition).getSubMenus().get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition).getTitle();
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_menu, null);
        }
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        ImageView expandedListTextViewIcon = (ImageView) convertView.findViewById(R.id.expandedListItemIcon);

        if( expandedListPosition == 0 ) {
            expandedListTextViewIcon.setImageDrawable(convertView.getResources().getDrawable(R.mipmap.icon_glass));
        } else {
            expandedListTextViewIcon.setImageDrawable(convertView.getResources().getDrawable(R.mipmap.icon_call));
        }
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        if( mExpandableListMenu.get(listPosition).getSubMenus() == null ) return 0;
        return mExpandableListMenu.get(listPosition).getSubMenus()
                .size();
    }

    @Override
    public ExpandableMenuListItem getGroup(int listPosition) {
        return mExpandableListMenu.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return mExpandableListMenu.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition).getTitle();
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        ExpandableMenuListItem menuItem = getGroup(listPosition);



        ImageView collapse = (ImageView) convertView.findViewById(R.id.list_collapse);
        menuItem.setCollapse(collapse);

        if( mExpandableListMenu.get(listPosition).getSubMenus() != null && mExpandableListMenu.get(listPosition).getSubMenus().size() > 0 ) {
            collapse.setVisibility(View.VISIBLE);
        } else {
            collapse.setVisibility(View.INVISIBLE);
        }

        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void rotate(int position) {
        float rotate = mExpandableListMenu.get(position).getRotate();
        float fromRotation = rotate;
        float toRotation = rotate += 180;

        ImageView imageview = mExpandableListMenu.get(position).getCollapse();

        final RotateAnimation rotateAnim = new RotateAnimation(
                fromRotation, toRotation, imageview.getWidth()/2, imageview.getHeight()/2);

        rotateAnim.setDuration(300); // Use 0 ms to rotate instantly
        rotateAnim.setFillAfter(true); // Must be true or the animation will reset

        imageview.startAnimation(rotateAnim);
        mExpandableListMenu.get(position).setRotate(toRotation);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
