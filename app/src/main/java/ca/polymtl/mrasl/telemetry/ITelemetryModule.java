package ca.polymtl.mrasl.telemetry;

import ca.polymtl.mrasl.payload.IPayload;
import ca.polymtl.mrasl.payload.PayloadManager;

/**
 * This interface is used for accessing the current payload a telemetry module.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface ITelemetryModule<P extends IPayload> {

    /**
     * This method returns the current payload saved in the telemetry module.
     *
     * @return The payload of the module
     */
    P getPayload();

    /**
     * This method sets the payload manager to send each new payload.
     *
     * @param manager
     */
    void setPayloadManager(PayloadManager manager);

}
