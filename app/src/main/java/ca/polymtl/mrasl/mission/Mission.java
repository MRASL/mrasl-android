package ca.polymtl.mrasl.mission;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ca.polymtl.mrasl.payload.PayloadCommand;
import ca.polymtl.mrasl.payload.PayloadManager;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.tag.TagList;

/**
 * This class represents the search and rescue mission of the drone.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Mission implements IDisposable {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = Mission.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final TagList fTagList = new TagList();
    private final FordHook fFordHook = new FordHook(this);
    private final List<MissionStateChangedListener> fListeners = new ArrayList<>();

    private PayloadManager fPayloadManager;
    private State fState;
    private long fStartTime;
    private long fStopTime;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public Mission() {
        fState = State.READY;
        fStartTime = 0L;
        fStopTime = 0L;
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method starts the search and rescue mission of the drone.
     *
     * @return {@code true} if the operation is successful, else {@code false}
     */
    public boolean startMission() {
        /* The drone must be ready */
        if (fState != State.READY) {
            return false;
        }

        /* Make sure we have a payload manager */
        if(fPayloadManager == null) {
            return false;
        }

        /* Set the command to the drone */
        fPayloadManager.setPayload(new PayloadCommand(PayloadCommand.CommandType.START_MISSION));

        /* Set the start time of the mission */
        fStartTime = System.currentTimeMillis();

        /* Change the state of the mission */
        fState = State.START_MISSION;

        /* Call the listeners that the state has changed */
        callListeners();

        return true;
    }

    /**
     * This method aborts the search and rescue mission by landing immediately.
     *
     * @return {@code true} if the operation is successful, else {@code false}
     */
    public boolean abortLanding() {
        /* The mission must be started to be aborted */
        if (fState != State.START_MISSION) {
            return false;
        }

        /* Make sure we have a payload manager */
        if(fPayloadManager == null) {
            return false;
        }

        /* Set the command to the drone */
        fPayloadManager.setPayload(new PayloadCommand(PayloadCommand.CommandType.ABORT_LANDING));

        /* Change the state of the mission */
        fState = State.ABORT_LANDING;

        /* Call the listeners that the state has changed */
        callListeners();

        return true;
    }

    /**
     * This method aborts the search and rescue mission by hovering the drone on place.
     *
     * @return {@code true} if the operation is successful, else {@code false}
     */
    public boolean abortMission() {
        /* The mission must be started to be aborted */
        if (fState != State.START_MISSION) {
            return false;
        }

        /* Make sure we have a payload manager */
        if(fPayloadManager == null) {
            return false;
        }

        /* Set the command to the drone */
        fPayloadManager.setPayload(new PayloadCommand(PayloadCommand.CommandType.ABORT_MISSION));

        /* Change the state of the mission */
        fState = State.ABORT_MISSION;

        /* Call the listeners that the state has changed */
        callListeners();

        return true;
    }

    /**
     * This method finishes the mission. The drone is totally immobile and its motors are turned
     * off.
     *
     * @return {@code true} if the operation is successful, else {@code false}
     */
    public boolean finishMission() {
        /* Make sure it can be stopped */
        if (fState == State.READY || fState == State.FINISHED) {
            return false;
        }

        Log.d(TAG, "The mission ended");

        /* Set the stop time of the mission */
        fStopTime = System.currentTimeMillis();

        /* Change the state of the mission */
        fState = State.FINISHED;

        /* Call the listeners that the state has changed */
        callListeners();

        return true;
    }

    /**
     * This method connects a listener that wants to know when the state of the mission changes. It
     * will call the listener a first time when it is added.
     *
     * @param listener The listener to connect
     */
    public void addMissionStateChangedListener(MissionStateChangedListener listener) {
        /* Make sure the listener isn't already connected */
        if (fListeners.contains(listener)) {
            return;
        }

        /* Add the listener to the list */
        fListeners.add(listener);

        /* Call the listener a first time */
        listener.onStateChanged(fState);
    }

    /**
     * This method disconnects a listener that wanted to know when the state of the mission
     * changed.
     *
     * @param listener The listener to remove
     */
    public void removeMissionStateChangedListener(MissionStateChangedListener listener) {
        /* Make sure the listener is connected */
        if (!fListeners.contains(listener)) {
            return;
        }

        /* Remove the listener to the list */
        fListeners.remove(listener);
    }

    /**
     * This method calls every listener that the state of the mission changed.
     */
    private void callListeners() {
        /* Call all the listeners in the list */
        for (MissionStateChangedListener listener : fListeners) {
            listener.onStateChanged(fState);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        fTagList.dispose();
        fFordHook.dispose();
    }

    // ---------------------------------------------------------------------------------------------
    // Mutators
    // ---------------------------------------------------------------------------------------------

    /**
     * This mutator sets the payload manager to send the command.
     *
     * @param manager The payload manager
     */
    public void setPayloadManager(PayloadManager manager) {
        fPayloadManager = manager;
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessor that returns the current state of the mission.
     *
     * @return The current state of the mission
     */
    public State getState() {
        return fState;
    }

    /**
     * Accessor that returns the start time of the mission. It will return zero if the mission isn't
     * started.
     *
     * @return The start time of the mission
     */
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * Accessor that returns the stop time of the mission. It will return zero if the mission isn't
     * stopped.
     *
     * @return The stop time of the mission
     */
    public long getStopTime() {
        return fStopTime;
    }

    /**
     * This accessor returns the list of detected tags of the mission.
     *
     * @return The tag list of the mission
     */
    public TagList getTagList() {
        return fTagList;
    }

}
