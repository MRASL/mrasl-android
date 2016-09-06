package ca.polymtl.mrasl.mission;

/**
 * This enumeration defines the possible state of the drone.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public enum State {

    /**
     * State when the drone is ready for the mission
     */
    READY,
    /**
     * State when the mission is started
     */
    START_MISSION,
    /**
     * State when the mission is aborted with a landing
     */
    ABORT_LANDING,
    /**
     * State when the mission is aborted by hovering
     */
    ABORT_MISSION,
    /**
     * State when the mission is finished
     */
    FINISHED;

}
