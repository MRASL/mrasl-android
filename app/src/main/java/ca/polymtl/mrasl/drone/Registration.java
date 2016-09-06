package ca.polymtl.mrasl.drone;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import static dji.sdk.SDKManager.DJISDKManager.DJISDKManagerCallback;

import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.R;
import dji.sdk.SDKManager.DJISDKManager;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;
import dji.sdk.base.DJISDKError;

/**
 * This class handles the DJI SDK for registering the application and connecting to a drone.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Registration implements DJISDKManagerCallback {

    private static Registration Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = Registration.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Context fContext;
    private final Handler fHandler = new Handler();

    private Aircraft fAircraft;
    private ArrayList<Runnable> fConnectionListeners = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public static Registration getInstance() {
        /* Setup the singleton instance */
        if (Instance == null) {
            Instance = new Registration();
        }

        return Instance;
    }

    Registration() {
        fContext = MainActivity.getInstance().getApplicationContext();

        /* Initialize DJI SDK Manager */
        DJISDKManager.getInstance().initSDKManager(fContext, this);
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method returns whether we are connected to the aircraft or not.
     *
     * @return {@code true} if we are connected, else {@code false}
     */
    public boolean isConnected() {
        return !(fAircraft == null);
    }

    /**
     * This method adds a listener to the list of listener that are called when the connection to a
     * drone changes. Adding a listener to the list will automatically fire the event. Your listener
     * should check if we have a connection using {@link #isConnected()} method.
     *
     * @param listener The listener to add to the list
     */
    public void addConnectionListener(Runnable listener) {
        /* Add the listener into the list */
        fConnectionListeners.add(listener);

        /* Call the listener a first time */
        fHandler.post(listener);
    }

    /**
     * This method removes a listener from the list of listener that are called when the connection
     * to a drone changes.
     *
     * @param listener The listener to remove from the list
     */
    public void removeConnectionListener(Runnable listener) {
        /* Remove the listener frm the list */
        if (fConnectionListeners.contains(listener)) {
            fConnectionListeners.remove(listener);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onGetRegisteredResult(DJIError error) {
        /* Check if the registration was a success */
        if (error != DJISDKError.REGISTRATION_SUCCESS) {
            Log.e(TAG, error.toString());

            /* Handle if the registration failed */
            fHandler.post(new RegistrationFailed());

            return;
        }

        /* Start the connection with the product */
        DJISDKManager.getInstance().startConnectionToProduct();

        /* Handle if the registration was successful */
        fHandler.post(new RegistrationSuccessful());
    }

    @Override
    public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {
        /* Create the aircraft object if possible */
        fAircraft = Aircraft.createAircraft(newProduct);

        /* Call every listener for a connection change */
        for (Runnable listener : fConnectionListeners) {
            fHandler.post(listener);
        }
    }

    /**
     * This class handles a successful registration.
     */
    private class RegistrationSuccessful implements Runnable {
        @Override
        public void run() {
            /* Show a message saying that registration was sucessful */
            String msg = fContext.getResources().getString(R.string.registration_success);
            Toast.makeText(fContext, msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This class handles a failed registration.
     */
    private class RegistrationFailed implements Runnable {
        @Override
        public void run() {
            /* Show a message saying that registration failed */
            String msg = fContext.getResources().getString(R.string.registration_failed);
            Toast.makeText(fContext, msg, Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * This accessors returns the currently connected aircraft. If will return {@code null} if no
     * aircraft is connected.
     *
     * @return The aircraft that is connected
     */
    public Aircraft getAicraft() {
        return fAircraft;
    }

}
