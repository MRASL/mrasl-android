package ca.polymtl.mrasl.ui.control;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.mission.MissionStateChangedListener;
import ca.polymtl.mrasl.mission.State;
import ca.polymtl.mrasl.shared.IDisposable;

/**
 * This class controls the timer of a mission.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class TimerControl implements IDisposable, Runnable, MissionStateChangedListener {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = TimerControl.class.getName();
    private static final int DELAY = 30;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Handler fHandler;
    private final Mission fMission;
    private final TextView fTimer;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public TimerControl(Activity activity, TextView timer, Mission mission) {
        fHandler = new Handler(activity.getMainLooper());
        fTimer = timer;
        fMission = mission;

        /* Add a mission state listener */
        fMission.addMissionStateChangedListener(this);
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method starts the timer.
     */
    private void start() {
        /* The timer will post itself at interval */
        fHandler.post(this);
    }

    /**
     * This method stops the timer.
     */
    private void stop() {
        /* Remove the callback of the timer */
        fHandler.removeCallbacks(this);
    }

    /**
     * This method changes the timestamp that is being showed by the text view.
     *
     * @param timestamp The timestamp value
     */
    private void setTimestamp(long timestamp) {
        /* Get the different units of the time */
        long h = TimeUnit.MILLISECONDS.toHours(timestamp) % 24;
        long m = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60;
        long s = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60;
        long ms = timestamp % 100;

        /* Format the timestamp */
        String text = String.format("%02d:%02d:%02d.%02d", h, m, s, ms);

        /* Change the text that is being showed */
        fTimer.setText(text);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Remove the mission state listener */
        fMission.removeMissionStateChangedListener(this);
    }

    @Override
    public void run() {
        /* Calculate the current time duration of the mission */
        long start = fMission.getStartTime();
        long now = System.currentTimeMillis();
        long diff = now - start;

        /* Show the time into the text view */
        setTimestamp(diff);

        /* Update the timer again later */
        fHandler.postDelayed(this, DELAY);
    }

    @Override
    public void onStateChanged(State state) {
        Log.d(TAG, "State change received");

        switch (state) {
            case START_MISSION:
                /* Start the timer */
                start();
                break;
            case ABORT_LANDING:
            case ABORT_MISSION:
                /* Remove and add the callback */
                stop();
                start();
                break;
            case FINISHED:
                /* Stop the timer */
                stop();

                /* Calculate the duration of the mission */
                long start = fMission.getStartTime();
                long end = fMission.getStopTime();
                long diff = end - start;

                /* Show the time into the text view */
                setTimestamp(diff);

                break;
        }
    }

}
