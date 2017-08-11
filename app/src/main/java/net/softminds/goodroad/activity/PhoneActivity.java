package net.softminds.goodroad.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import net.softminds.goodroad.R;
import net.softminds.goodroad.adapter.PhoneListAdapter;
import net.softminds.goodroad.adapter.PhoneNumberListAdapter;
import net.softminds.goodroad.fragment.Phone1Fragment;
import net.softminds.goodroad.fragment.Phone2Fragment;

public class PhoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        ImageButton ibBack = (ImageButton) findViewById(R.id.btn_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(getTitle());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        PhoneListAdapter adapter = new PhoneListAdapter(getSupportFragmentManager());
        Phone1Fragment fragment1 = new Phone1Fragment();
        Phone2Fragment fragment2 = new Phone2Fragment();
        adapter.addFragment(fragment1, "동물 보호 센터 전화");
        adapter.addFragment(fragment2, "사체 처리 신고 접수");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
