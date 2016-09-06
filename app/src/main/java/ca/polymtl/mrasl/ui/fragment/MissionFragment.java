package ca.polymtl.mrasl.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.drone.Aircraft;
import ca.polymtl.mrasl.drone.Registration;
import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.ui.control.MissionControl;
import ca.polymtl.mrasl.ui.control.TimerControl;
import dji.sdk.base.DJIBaseProduct;

/**
 * This class reports and controls the mission.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class MissionFragment extends Fragment {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int INTERVAL = 30;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // --------------------------------------------------------------------------------------------

    private final Context fContext;
    private final Handler fHandler = new Handler();

    private Mission fMission;
    private MissionControl fControl;
    private TimerControl fTimerControl;
    private Runnable fConnectionListener;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public MissionFragment() {
        fContext = MainActivity.getInstance().getApplicationContext();
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.content_mission, container, false);

        /* Add a listener for a connection change */
        fConnectionListener = new ConnectionChangedListener(view);
        Registration.getInstance().addConnectionListener(fConnectionListener);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* Dispose the mission control is possible */
        if (fControl != null) {
            fControl.dispose();
        }

        /* Dispose the timer control is possible */
        if (fTimerControl != null) {
            fTimerControl.dispose();
        }

        /* Remove the listener for a connection change */
        Registration.getInstance().removeConnectionListener(fConnectionListener);
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * The class listens for connection change.
     */
    private class ConnectionChangedListener implements Runnable {
        private final TextView fStatus;
        private final TextView fDrone;

        private ConnectionChangedListener(View view) {
            fStatus = (TextView) view.findViewById(R.id.mission_status);
            fDrone = (TextView) view.findViewById(R.id.mission_drone);
        }

        @Override
        public void run() {
            String status;
            String drone;

            /* Get the mission planner */
            Aircraft aircraft = Registration.getInstance().getAicraft();
            if (aircraft != null) {
                fMission = aircraft.getMission();
            } else {
                fMission = null;
            }

            /* Update the text */
            if (fMission != null) {
                DJIBaseProduct product = aircraft.getProduct();

                /* Create the mission control */
                Activity act = MainActivity.getInstance();
                Button start = (Button) getView().findViewById(R.id.button_start_mission);
                Button abortLanding = (Button) getView().findViewById(R.id.button_abort_landing);
                Button abortMission = (Button) getView().findViewById(R.id.button_abort_mission);
                fControl = new MissionControl(act, fMission, start, abortLanding, abortMission);

                /* Create the timer control */
                TextView timer = (TextView) getView().findViewById(R.id.text_time);
                fTimerControl = new TimerControl(MainActivity.getInstance(), timer, fMission);

                /* The connection was successful */
                status = fContext.getResources().getString(R.string.aircraft_connected);
                drone = product.getModel().getDisplayName();
            } else {
                /* Remove the mission control */
                if (fControl != null) {
                    fControl.dispose();
                    fControl = null;
                }

                /* Remove the timer control */
                if (fTimerControl != null) {
                    fTimerControl.dispose();
                    fTimerControl = null;
                }

                /* The connection failed */
                status = fContext.getResources().getString(R.string.aircraft_disconnected);
                drone = fContext.getResources().getString(R.string.aircraft_none);
            }

            /* Update the text view */
            fStatus.setText(status);
            fDrone.setText(drone);
        }
    }

}
