package ca.polymtl.mrasl.ui.activity;

import static android.view.View.OnSystemUiVisibilityChangeListener;
import static dji.sdk.Battery.DJIBattery.DJIBatteryStateUpdateCallback;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.drone.Aircraft;
import ca.polymtl.mrasl.drone.Camera;
import ca.polymtl.mrasl.drone.Registration;
import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.tag.TagList;
import ca.polymtl.mrasl.ui.control.MissionControl;
import ca.polymtl.mrasl.ui.control.TagTableControl;
import ca.polymtl.mrasl.ui.control.TimerControl;

import dji.sdk.Battery.DJIBattery.DJIBatteryState;

/**
 * This class is an aggregation of all the other fragments. It should be used on a tablet.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class CommandCenterActivity extends AppCompatActivity implements
        DJIBatteryStateUpdateCallback {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int HIDE_BUTTON_DELAY = 3000;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Handler fHandler = new Handler();
    private final Runnable fListener = new ConnectionChangeListener();

    private Aircraft fAircraft;
    private TagTableControl fTable;
    private MissionControl fControl;
    private TimerControl fTimer;

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Remove title bar */
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /* Remove notification bar*/
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, flag);

        /* Load activity view */
        setContentView(R.layout.activity_command_center);

        /* Hide the navigation buttons */
        fHandler.post(new HideNavigationButton());

        /* Add a listener that will keep the button hidden */
        OnSystemUiVisibilityChangeListener listener = new SystemUIChangeListener();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(listener);

        /* Add the listener that fix the aspect ratio of the video feed */
        TextureView view = (TextureView) findViewById(R.id.video_feed);
        view.post(new FixVideoTextureAspectRatio());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Dispose the tag table if possible */
        if (fTable != null) {
            fTable.dispose();
        }

        /* Dispose the mission control if possible */
        if (fControl != null) {
            fControl.dispose();
        }

        /* Dispose the timer control if possible */
        if (fTimer != null) {
            fTimer.dispose();
        }

        /* Remove the battery listener */
        if (fAircraft != null) {
            fAircraft.getBattery().setBatteryStateUpdateCallback(null);
        }

        /* Unregister the connection listener */
        Registration.getInstance().removeConnectionListener(fListener);
    }

    @Override
    public void onResult(DJIBatteryState battery) {
        /* Update the text shown on the screen */
        TextView view = (TextView) findViewById(R.id.battery);
        view.setText(String.valueOf(battery.getBatteryEnergyRemainingPercent()) + " %");
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class hides the navigation buttons.
     */
    private class HideNavigationButton implements Runnable {
        @Override
        public void run() {
            int options = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            getWindow().getDecorView().setSystemUiVisibility(options);
        }
    }

    /* This class fixes the aspect ratio of the video texture. The video texture should have an
     * aspect ratio of 16:9.
     */
    private class FixVideoTextureAspectRatio implements Runnable {
        @Override
        public void run() {
            /* Get the view that displays the image */
            RelativeLayout view = (RelativeLayout) findViewById(R.id.video_feed_frame);

            /* Set the view aspect ratio to the same of the image */
            LayoutParams params = view.getLayoutParams();
            params.width = (int) ((view.getHeight() * 1280.0) / 720.0);

            /* Register the connection listener only when the resize has been done. */
            /* It fixes a problem where the video feed would be cropped. */
            Registration.getInstance().addConnectionListener(fListener);
        }
    }

    /**
     * This class handles changes in the UI. It will make sure that the navigation buttons are hid
     * after a certain amount of time.
     */
    private class SystemUIChangeListener implements OnSystemUiVisibilityChangeListener {
        private final Runnable fHideButtons = new HideNavigationButton();

        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            fHandler.postDelayed(fHideButtons, HIDE_BUTTON_DELAY);
        }
    }

    /**
     * This class handles the change of connection to an aircraft.
     */
    private class ConnectionChangeListener implements Runnable {
        private void connect() {
            /* Make sure that an aircraft is connected */
            fAircraft = Registration.getInstance().getAicraft();
            if (fAircraft == null) {
                return;
            }

            /* Get the mission and the tag list */
            Mission mission = fAircraft.getMission();
            TagList list = mission.getTagList();

            /* Create the table control */
            TableLayout table = (TableLayout) findViewById(R.id.table_tags);
            ImageView image = (ImageView) findViewById(R.id.image_tag);
            fTable = new TagTableControl(CommandCenterActivity.this, list, image, table);

            /* Create the mission control */
            Activity act = CommandCenterActivity.this;
            Button startMission = (Button) findViewById(R.id.start_mission);
            Button abortLanding = (Button) findViewById(R.id.abort_landing);
            Button abortMission = (Button) findViewById(R.id.abort_mission);
            fControl = new MissionControl(act, mission, startMission, abortLanding, abortMission);

            /* Create the timer control */
            TextView timer = (TextView) findViewById(R.id.time);
            fTimer = new TimerControl(CommandCenterActivity.this, timer, mission);

            /* Add the battery callback */
            fAircraft.getBattery().setBatteryStateUpdateCallback(CommandCenterActivity.this);

            /* Make sure that the aircraft has a camera */
            Camera camera = fAircraft.getCamera();
            if (camera == null) {
                return;
            }

            /* Setup our texture to receive the video feed */
            TextureView texture = (TextureView) findViewById(R.id.video_feed);
            if (texture != null) {
                camera.setVideoSurface(texture);
            }
        }

        @Override
        public void run() {
            connect();
        }
    }

}
