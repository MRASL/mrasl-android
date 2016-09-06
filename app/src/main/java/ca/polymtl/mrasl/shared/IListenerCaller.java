package ca.polymtl.mrasl.shared;

/**
 * This interface defines a object that can contain and call listeners.
 *
 * @param <L> The type of listener it can call
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IListenerCaller<L> {

    /**
     * This method connects a listener to a specific event.
     *
     * @param listener The listener to add
     */
    void addListener(L listener);

    /**
     * This method disconnects a listener from a specific event.
     *
     * @param listener The listener to remove
     */
    void removeListener(L listener);

}
