package ca.polymtl.mrasl.payload;

/**
 * This interface is used for getting information about a payload. Any class that implements this
 * interface should only format the payload when the {@link #getPayload()} method is called.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IPayload {

    /**
     * This method returns the payload itself. It should build the payload only when it is called
     * because the payload might be rejected if the timestamp if too old.
     *
     * @return The byte representation of the payload
     */
    byte[] getPayload();

}
