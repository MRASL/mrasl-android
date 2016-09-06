package ca.polymtl.mrasl.mission;

/**
 * This interface defines a callback when the state of the mission changes.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface MissionStateChangedListener {

    /**
     * This method is called when the state of the mission changed.
     *
     * @param state The new state of the mission
     */
    void onStateChanged(State state);

}
