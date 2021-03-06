package kr.co.goodroad.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by hjlee on 2017-08-11.
 */

public class PhoneListAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public PhoneListAdapter(FragmentManager fm)
    {
        super(fm);
    }



    public void addFragment(Fragment fragment, String title)
    {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position)
    {
        return mFragments.get(position);
    }

    @Override
    public int getCount()
    {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return mFragmentTitles.get(position);
    }
}
