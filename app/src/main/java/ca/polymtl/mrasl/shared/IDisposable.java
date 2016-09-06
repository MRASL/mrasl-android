package ca.polymtl.mrasl.shared;

/**
 * This interface is used for disposing object's resources.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDisposable {

    /**
     * This method disposes the object from all of his listeners, broadcast receivers, ROS nodes,
     * etc.
     */
    void dispose();

}
