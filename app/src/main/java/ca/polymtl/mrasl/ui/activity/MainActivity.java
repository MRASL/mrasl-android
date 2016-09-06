package ca.polymtl.mrasl.ui.activity;

import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.drone.Aircraft;
import ca.polymtl.mrasl.drone.Registration;
import ca.polymtl.mrasl.ros.RosConnection;
import ca.polymtl.mrasl.ui.fragment.CameraFragment;
import ca.polymtl.mrasl.ui.fragment.MissionFragment;
import ca.polymtl.mrasl.ui.fragment.TelemetryFragment;

/**
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    private static MainActivity Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = MainActivity.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private DrawerLayout fDrawer;
    private FragmentManager fFragmentManager;
    private ActionBarDrawerToggle fDrawerToggle;
    private NavigationView fNavigation;
    private Toolbar fToolbar;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public static MainActivity getInstance() {
        return Instance;
    }

    public MainActivity() {
        /* Setup the singleton instance */
        if (Instance == null) {
            Instance = this;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Request the following permissions */
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.READ_PHONE_STATE,
                }, 1);

        /* Replace the ActionBar by our toolbar */
        fToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(fToolbar);

        /* Add the drawer button */
        fDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        fDrawerToggle = new ActionBarDrawerToggle(this, fDrawer, fToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        fDrawer.addDrawerListener(fDrawerToggle);
        fDrawerToggle.syncState();

        /* Setup the navigation view */
        fNavigation = (NavigationView) findViewById(R.id.nvView);
        fNavigation.setNavigationItemSelectedListener(this);

        /* Set the default fragment to the mission */
        fNavigation.getMenu().getItem(0).setChecked(true);
        fFragmentManager = getFragmentManager();
        fFragmentManager.beginTransaction().replace(R.id.flContent, new MissionFragment()).commit();

        /* Start the DJI registration instance */
        Registration.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* Shutdown ROS node */
        RosConnection.getInstance().shutdown();

        /* Dispose the aircraft */
        Aircraft aircraft = Registration.getInstance().getAicraft();
        if(aircraft != null) {
            aircraft.dispose();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        /* Close the drawer to the left if opened */
        if (fDrawer.isDrawerOpen(GravityCompat.START)) {
            fDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        /* The action bar home button should open the drawer */
        switch (item.getItemId()) {
            case android.R.id.home:
                fDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_command_center:
                Intent commandIntent = new Intent(this, CommandCenterActivity.class);
                startActivity(commandIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        /* Inflate the toolbar */
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        /* Change the color of every item icon */
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();

            /* Make sure the item has an icon */
            if (drawable == null) {
                continue;
            }

            /* Get the color that we want */
            int color = ContextCompat.getColor(this, R.color.colorUnselected);

            /* Change the color of the icon */
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;

        /* Handle the navigation of the drawer */
        switch (item.getItemId()) {
            case R.id.nav_mission:
                fragment = new MissionFragment();
                break;
            case R.id.nav_telemetry:
                fragment = new TelemetryFragment();
                break;
            case R.id.nav_camera:
                fragment = new CameraFragment();
                break;
        }

        /* Change the fragment */
        fFragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        /* We loop to unselect old selections */
        for (int i = 0; i < fNavigation.getMenu().size(); i++) {
            fNavigation.getMenu().getItem(i).setChecked(false);
        }

        /* Change the title and the selection of the drawer */
        item.setChecked(true);
        setTitle(item.getTitle());

        /* Close the drawer after selection */
        fDrawer.closeDrawers();

        return true;
    }

}
