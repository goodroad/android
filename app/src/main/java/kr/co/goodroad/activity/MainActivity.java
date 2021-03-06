package kr.co.goodroad.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import kr.co.goodroad.R;
import kr.co.goodroad.adapter.CustomExpandableListAdapter;
import kr.co.goodroad.common.Definitions;
import kr.co.goodroad.fragment.MainFragment;
import kr.co.goodroad.fragment.ReportCompleteFragment;
import kr.co.goodroad.model.ExpandableMenuListItem;
import kr.co.goodroad.util.SMHttpClient;
import kr.co.goodroad.util.UriUtil;

import org.json.JSONObject;

import java.io.File;
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
    static public String localNumber = null;
    private Thread mThreadLocation;
    public static Location mCurrentLocation = null;
    private LocationManager mLocationManager = null;
    private MainActivity self;

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
            Log.d(TAG, "[LOCATION] Current location : lat(" + location.getLatitude() + ") lng(" + location.getLongitude() + ")");


            if (mCurrentLocation == null) {
                Log.d(TAG, "[LOCATION] mCurrentLocation is null");
                mCurrentLocation = location;
                if (mLocationChangeListner != null) {
                    mLocationChangeListner.onLocationChanged(location);
                }

                mThreadLocation = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject addr = SMHttpClient.execute("GET", "https://apis.daum.net/local/geo/coord2addr", null,
                                    "apikey=" + Definitions.DAUM_REST_API_KEY + "&longitude=" + location.getLongitude() + "&latitude=" + location.getLatitude() + "&inputCoordSystem=WGS84&output=json", null);
                            if (addr != null) {
                                final String fullName = addr.getString("fullName");

                                if (fullName != null) {
                                    if (fullName.contains("서울")) {
                                        localNumber = "02";
                                    } else if (fullName.contains("경기도")) {
                                        localNumber = "031";
                                    } else if (fullName.contains("인천")) {
                                        localNumber = "032";
                                    } else if (fullName.contains("강원도")) {
                                        localNumber = "033";
                                    } else if (fullName.contains("충청남도")) {
                                        localNumber = "041";
                                    } else if (fullName.contains("대전")) {
                                        localNumber = "042";
                                    } else if (fullName.contains("충청북도")) {
                                        localNumber = "043";
                                    } else if (fullName.contains("세종")) {
                                        localNumber = "044";
                                    } else if (fullName.contains("개성")) {
                                        localNumber = "049";
                                    } else if (fullName.contains("부산")) {
                                        localNumber = "051";
                                    } else if (fullName.contains("울산")) {
                                        localNumber = "052";
                                    } else if (fullName.contains("대구")) {
                                        localNumber = "053";
                                    } else if (fullName.contains("경상북도")) {
                                        localNumber = "054";
                                    } else if (fullName.contains("경상남도")) {
                                        localNumber = "055";
                                    } else if (fullName.contains("전라남도")) {
                                        localNumber = "061";
                                    } else if (fullName.contains("광주")) {
                                        localNumber = "062";
                                    } else if (fullName.contains("전라북도")) {
                                        localNumber = "063";
                                    } else if (fullName.contains("제주")) {
                                        localNumber = "064";
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            mThreadLocation = null;
                        }
                    }
                });
                mThreadLocation.start();

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

        self = this;
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

//        ImageButton btnOpenNotice = (ImageButton) findViewById(R.id.btn_open_notice);
//        btnOpenNotice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent mainIntent = new Intent(MainActivity.this, NoticeActivity.class);
//                MainActivity.this.startActivity(mainIntent);
//            }
//        });

        mNavigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_content, MainFragment.getInstance()).commit();
        }


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkWritePermission();
    }

    private void checkWritePermission() {
        Log.d(TAG, "[PERMISSION] check write permission!!");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            checkLocationPermssion();
        }
    }

    private void checkLocationPermssion() {
        Log.d(TAG, "[LOCATION] checkLocationPermssion!!");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Log.d(TAG,"[LOCATION] requestLocationUpdates ");
            if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        5000, 0, mLocationListener);
            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5000, 0, mLocationListener);
        }
    }


    public void setToolbarTitle(int resource) {
        TextView tvTitle = (TextView) findViewById(R.id.toolbar_title);
        tvTitle.setText(resource);
    }

    private void addDrawerItems() {
        Log.d(TAG, "addDrawerItems");
        mExpandableListAdapter = new CustomExpandableListAdapter(this, mExpandableListMenus);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (mExpandableListAdapter.getChildrenCount(groupPosition) == 0) {
                    openMenu(groupPosition, -1);
                    Log.d(TAG, "CLOSEDRAWER #1");
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mExpandableListAdapter.rotate(groupPosition);
                }
            }
        });

        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Log.d(TAG, "mExpandableListView setOnGroupCollapseListener : " + groupPosition);
                if (mExpandableListAdapter.getChildrenCount(groupPosition) == 0) {
                    openMenu(groupPosition, -1);
                    Log.d(TAG, "CLOSEDRAWER #2");
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
                Log.d(TAG, "mExpandableListView setOnChildClickListener : " + groupPosition + "-" + childPosition);
                openMenu(groupPosition, childPosition);
                Log.d(TAG, "CLOSEDRAWER #3");
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    private void openMenu(int groupPosition, int childPosition) {
        if (mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_introduction))) {
            Intent mainIntent = new Intent(MainActivity.this, IntroActivity.class);
            MainActivity.this.startActivity(mainIntent);
        } else if (mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_news))) {
            Intent mainIntent = new Intent(MainActivity.this, NewsActivity.class);
            MainActivity.this.startActivity(mainIntent);
        } else if (mExpandableListMenus.get(groupPosition).getTitle().equals(getString(R.string.menu_roadkill_method))) {
            if (mExpandableListMenus.get(groupPosition).getSubMenus().get(childPosition).getTitle().equals(getString(R.string.menu_roadkill_manual))) {
                Intent mainIntent = new Intent(MainActivity.this, ManualActivity.class);

                mainIntent.setAction(Intent.ACTION_VIEW);
                mainIntent.setDataAndType(Uri.parse("android.resource://kr.co.goodroad/raw/manual"), "application/pdf");
                MainActivity.this.startActivity(mainIntent);
            } else if (mExpandableListMenus.get(groupPosition).getSubMenus().get(childPosition).getTitle().equals(getString(R.string.menu_roadkill_phone))) {
                Intent mainIntent = new Intent(MainActivity.this, PhoneActivity.class);
                MainActivity.this.startActivity(mainIntent);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private List<ExpandableMenuListItem> getList() {
        List<ExpandableMenuListItem> expandableListData = new ArrayList<ExpandableMenuListItem>();


        PopupMenu p = new PopupMenu(this, null);
        Menu menus = p.getMenu();
        getMenuInflater().inflate(R.menu.activity_main_drawer, menus);

        for (int i = 0; i < menus.size(); i++) {
            ExpandableMenuListItem menu = new ExpandableMenuListItem();
            menu.setTitle(menus.getItem(i).getTitle().toString());
            expandableListData.add(menu);

            if (menus.getItem(i).hasSubMenu()) {
                List<ExpandableMenuListItem> submenus = new ArrayList<ExpandableMenuListItem>();

                for (int j = 0; j < menus.getItem(i).getSubMenu().size(); j++) {
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
        Log.d(TAG, "CLOSEDRAWER #5");
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
                        Log.d(TAG, "CLOSEDRAWER #6");
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG,"[LOCATION] onRequestPermissionsResult requestCode(" + requestCode + ") ");
        if (requestCode == 2) {
            checkLocationPermssion();
        } else if (requestCode == 1) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"[LOCATION] Location is not permitted ");
                return;
            }
            Log.d(TAG,"[LOCATION] requestLocationUpdates ");
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    5000, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 0, mLocationListener);

        }
    }
}
