package net.softminds.goodroad.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import net.softminds.goodroad.R;
import net.softminds.goodroad.adapter.CustomExpandableListAdapter;
import net.softminds.goodroad.fragment.MainFragment;
import net.softminds.goodroad.fragment.ReportCompleteFragment;
import net.softminds.goodroad.model.ExpandableMenuListItem;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by hjlee on 2017-07-30.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnFragmentInteractionListener, ReportCompleteFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getName();
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ExpandableListView mExpandableListView;
    private CustomExpandableListAdapter mExpandableListAdapter;
    private List<ExpandableMenuListItem> mExpandableListMenus;

    public static Location mCurrentLocation = null;
    private LocationManager mLocationManager = null;


    static public interface OnLocationChangeListener {
        void onLocationChanged(final Location location);
    }

    public static OnLocationChangeListener getLocationChangeListner() {
        return mLocationChangeListner;
    }

    public static void setLocationChangeListner(OnLocationChangeListener locationChangeListner) {
        mLocationChangeListner = locationChangeListner;
    }

    private static OnLocationChangeListener mLocationChangeListner;




    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d(TAG, "Current location : lat(" + location.getLatitude() + ") lng(" + location.getLongitude() + ")");
            if (mCurrentLocation == null) {
                Log.d(TAG, "mCurrentLocation is null");
                mCurrentLocation = location;
                if( mLocationChangeListner != null ) {
                    mLocationChangeListner.onLocationChanged(location);
                }
            } else {
                Log.d(TAG, "mCurrentLocation is not null");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        mExpandableListView = (ExpandableListView) findViewById(R.id.navList);
        mExpandableListMenus = getList();

        addDrawerItems();

        ImageButton btnOpenNotice = (ImageButton) findViewById(R.id.btn_open_notice);
        btnOpenNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, NoticeActivity.class);
                MainActivity.this.startActivity(mainIntent);
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_content, MainFragment.getInstance()).commit();
        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                5000, 100, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 100, mLocationListener);
    }

    public void setToolbarTitle(int resource) {
        TextView tvTitle = (TextView) findViewById(R.id.toolbar_title);
        tvTitle.setText(resource);
    }

    private void addDrawerItems() {
        Log.d(TAG,"addDrawerItems");
        mExpandableListAdapter = new CustomExpandableListAdapter(this, mExpandableListMenus);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if( mExpandableListAdapter.getChildrenCount(groupPosition) == 0 ) {
                    openMenu(groupPosition,-1);
                    Log.d(TAG,"CLOSEDRAWER #1");
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mExpandableListAdapter.rotate(groupPosition);
                }
            }
        });

        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Log.d(TAG,"mExpandableListView setOnGroupCollapseListener : " + groupPosition);
                if( mExpandableListAdapter.getChildrenCount(groupPosition) == 0 ) {
                    openMenu(groupPosition,-1);
                    Log.d(TAG,"CLOSEDRAWER #2");
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mExpandableListAdapter.rotate(groupPosition);
                }
            }
        });

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Log.d(TAG,"mExpandableListView setOnChildClickListener : " + groupPosition + "-" + childPosition);
                openMenu(groupPosition,childPosition);
                Log.d(TAG,"CLOSEDRAWER #3");
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    private void openMenu(int groupPosition, int childPosition) {
        if( mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_introduction)) ) {
            Intent mainIntent = new Intent(MainActivity.this, IntroActivity.class);
            MainActivity.this.startActivity(mainIntent);
        } else if( mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_news)) ) {
            Intent mainIntent = new Intent(MainActivity.this, NewsActivity.class);
            MainActivity.this.startActivity(mainIntent);
        } else if( mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_roadkill_method)) ) {
            if( mExpandableListMenus.get(groupPosition).getSubMenus().get(childPosition).getTitle().equals(getString(R.string.menu_roadkill_manual))) {
                Intent mainIntent = new Intent(MainActivity.this, ManualActivity.class);
                MainActivity.this.startActivity(mainIntent);
            } else if( mExpandableListMenus.get(groupPosition).getSubMenus().get(childPosition).getTitle().equals(getString(R.string.menu_roadkill_phone))) {
                Intent mainIntent = new Intent(MainActivity.this, PhoneActivity.class);
                MainActivity.this.startActivity(mainIntent);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private List<ExpandableMenuListItem> getList() {
        List<ExpandableMenuListItem> expandableListData = new ArrayList<ExpandableMenuListItem>();


        PopupMenu p  = new PopupMenu(this, null);
        Menu menus = p.getMenu();
        getMenuInflater().inflate(R.menu.activity_main_drawer, menus);

        for ( int i = 0; i < menus.size() ; i++ ) {
            ExpandableMenuListItem menu = new ExpandableMenuListItem();
            menu.setTitle(menus.getItem(i).getTitle().toString());
            expandableListData.add(menu);

            if( menus.getItem(i).hasSubMenu() ) {
                List<ExpandableMenuListItem> submenus = new ArrayList<ExpandableMenuListItem>();

                for (int j = 0; j < menus.getItem(i).getSubMenu().size() ; j++) {
                    ExpandableMenuListItem submenu = new ExpandableMenuListItem();
                    submenu.setTitle(menus.getItem(i).getSubMenu().getItem(j).getTitle().toString());
                    submenus.add(submenu);
                }
                expandableListData.get(i).setSubMenus(submenus);
            }
        }
        return expandableListData;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_introduction) {
            // Handle the camera action
        } else if (id == R.id.nav_news) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Log.d(TAG,"CLOSEDRAWER #5");
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        Log.d(TAG,"CLOSEDRAWER #6");
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
}
