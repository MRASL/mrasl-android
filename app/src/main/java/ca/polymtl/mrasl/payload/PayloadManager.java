package ca.polymtl.mrasl.payload;

import android.os.Handler;
import android.util.Log;

import ca.polymtl.mrasl.drone.RadioLink;

/**
 * This class manages {@link IPayload} that has to be sent through a {@link RadioLink}.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PayloadManager {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = PayloadManager.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Handler fHandler = new Handler();

    private Runnable fListener;
    private IPayload fPayloadGPS;
    private IPayload fPayloadSensors;
    private IPayload fPayloadCommand;
    private boolean fReady = false;

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method changes the current GPS payload to send.
     *
     * @param payload The new GPS payload
     */
    public void setPayload(PayloadGPS payload) {
        fPayloadGPS = payload;
    }

    /**
     * This method changes the current sensors payload to send.
     *
     * @param payload The new sensors payload
     */
    public void setPayload(PayloadSensors payload) {
        fPayloadSensors = payload;

        /**
         * Set the payload manager as ready if it was not.
         */
        if (!fReady) {
            Log.d(TAG, "The payload manager is ready.");
            fReady = true;

            /* Send a broadcast that the payload manager is ready */
            if (fListener != null) {
                fHandler.post(fListener);
            }
        }
    }

    /**
     * This method change the current command payload to send.
     *
     * @param payload The new command payload
     */
    public void setPayload(PayloadCommand payload) {
        fPayloadCommand = payload;
    }

    /**
     * This method returns the next payload to send. A command payload has the highest priority. It
     * will always be sent before before the GPS that has the second priority or the sensors, that
     * has the lowest priority.
     *
     * @return The payload to send
     */
    public IPayload getNext() {
        IPayload payload;

        /* Find which payload to send */
        if (fPayloadCommand != null) {
            payload = fPayloadCommand;
            fPayloadCommand = null;
        } else if (fPayloadGPS != null) {
            payload = fPayloadGPS;
            fPayloadGPS = null;
        } else {
            /**
             * We don't set the sensors to {@code null} because we don't want to fire the ready
             * listener too often. Considering the speed of the sensors, it is very unlikely that
             * we will send twice the same information.
             */
            payload = fPayloadSensors;
        }

        return payload;
    }

    /**
     * This method requests that the payload manager to resend a broadcast signal telling that is it
     * ready.
     */
    public void requestReady() {
        Log.d(TAG, "The payload manager is restarting.");
        fReady = false;

        /* Remove current payloads */
        fPayloadCommand = null;
        fPayloadGPS = null;
        fPayloadSensors = null;
    }

    // ---------------------------------------------------------------------------------------------
    // Mutators
    // ---------------------------------------------------------------------------------------------

    /**
     * This mutators changes the readiness listener that is fired when the payload manager is ready
     * to send a payload. To remove the listener, simply set the listener to {@code null}.
     *
     * @param listener The new listener
     */
    public void setReadyListener(Runnable listener) {
        fListener = listener;
    }

}
