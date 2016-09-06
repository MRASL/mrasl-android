package ca.polymtl.mrasl.ui.control;

import static android.app.AlertDialog.Builder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.mission.MissionStateChangedListener;
import ca.polymtl.mrasl.mission.State;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.ui.activity.MainActivity;

/**
 * This class handles the UI part for controlling a mission.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class MissionControl implements
        IDisposable,
        View.OnClickListener,
        DialogInterface.OnClickListener,
        MissionStateChangedListener {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = MissionControl.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Handler fHandler;
    private final Mission fMission;
    private final Button fStartMission;
    private final Button fAbortLanding;
    private final Button fAbortMission;
    private final AlertDialog fStartMissionDialog;
    private final AlertDialog fAbortLandingDialog;
    private final AlertDialog fAbortMissionDialog;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor for a mission control.
     *
     * @param activity     The activity that has the button
     * @param mission      The mission object that contains the state
     * @param startMission The start mission button
     * @param abortLanding The abort landing button
     * @param abortMission The abort mission button
     */
    public MissionControl(Activity activity,
                          Mission mission,
                          Button startMission,
                          Button abortLanding,
                          Button abortMission) {
        fHandler = new Handler(activity.getMainLooper());
        fMission = mission;
        fStartMission = startMission;
        fAbortLanding = abortLanding;
        fAbortMission = abortMission;

        /* Add the listener to the buttons */
        fStartMission.setOnClickListener(this);
        fAbortLanding.setOnClickListener(this);
        fAbortMission.setOnClickListener(this);

        /* Add a mission state listener */
        fMission.addMissionStateChangedListener(this);

        /* Create a base builder for all the dialogs */
        Context context = MainActivity.getInstance().getApplicationContext();
        Builder builder = new Builder(activity)
                .setTitle("Warning")
                .setPositiveButton("Yes", this)
                .setNegativeButton("No", this);

        /* Create the dialog for "start mission" */
        fStartMissionDialog = builder
                .setMessage(context.getResources().getString(R.string.dialog_start_mission))
                .create();

        /* Create the dialog for "abort landing" */
        fAbortLandingDialog = builder
                .setMessage(context.getResources().getString(R.string.dialog_abort_landing))
                .create();

        /* Create the dialog for "abort mission" */
        fAbortMissionDialog = builder
                .setMessage(context.getResources().getString(R.string.dialog_abort_mission))
                .create();
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Remove the listener to the buttons */
        fStartMission.setOnClickListener(null);
        fAbortLanding.setOnClickListener(null);
        fAbortMission.setOnClickListener(null);

        /* Remove the mission state listener */
        fMission.removeMissionStateChangedListener(this);
    }

    @Override
    public void onClick(View view) {
        /* Ask the user for confirmation */
        if (view == fStartMission) {
            fStartMissionDialog.show();
        } else if (view == fAbortLanding) {
            fAbortLandingDialog.show();
        } else if (view == fAbortMission) {
            fAbortMissionDialog.show();
        } else {
            return;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int button) {
        boolean success;

        /* Make sure the user accepted */
        if (button == DialogInterface.BUTTON_NEGATIVE) {
            return;
        }

        /* Execute the command */
        if (dialog == fStartMissionDialog) {
            success = fMission.startMission();
        } else if (dialog == fAbortLandingDialog) {
            success = fMission.abortLanding();
        } else if (dialog == fAbortMissionDialog) {
            success = fMission.abortMission();
        } else {
            return;
        }

        /* Make sure the command was executed */
        if (!success) {
            return;
        }

        /* Update the controls */
        fHandler.post(new UpdateControls());
    }

    @Override
    public void onStateChanged(State state) {
        Log.d(TAG, "State change received");

        /* Update the controls */
        fHandler.post(new UpdateControls());
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class update the controls in the UI.
     */
    private class UpdateControls implements Runnable {
        @Override
        public void run() {
            /* Update the buttons based on the state */
            switch (fMission.getState()) {
                case READY:
                    fStartMission.setEnabled(true);
                    fAbortLanding.setEnabled(false);
                    fAbortMission.setEnabled(false);
                    break;
                case START_MISSION:
                    fStartMission.setEnabled(false);
                    fAbortLanding.setEnabled(true);
                    fAbortMission.setEnabled(true);
                    break;
                case ABORT_LANDING:
                case ABORT_MISSION:
                case FINISHED:
                    fStartMission.setEnabled(false);
                    fAbortLanding.setEnabled(false);
                    fAbortMission.setEnabled(false);
                    break;
            }
        }
    }

}