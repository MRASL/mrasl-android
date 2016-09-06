package ca.polymtl.mrasl.drone;

import static dji.sdk.base.DJIBaseComponent.DJICompletionCallback;
import static dji.sdk.FlightController.DJIFlightControllerDelegate.FlightControllerReceivedDataFromExternalDeviceCallback;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;

import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.payload.IPayload;
import ca.polymtl.mrasl.payload.PayloadManager;
import ca.polymtl.mrasl.telemetry.GPSModule;
import ca.polymtl.mrasl.telemetry.SensorsModule;
import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIError;

/**
 * This class represents the radio link used for communicating commands and telemetry to the drone.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class RadioLink implements
        IDisposable,
        FlightControllerReceivedDataFromExternalDeviceCallback,
        DJICompletionCallback,
        OnSharedPreferenceChangeListener {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = RadioLink.class.getName();
    private static final int RESTART_DELAY = 2000;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final HandlerThread fThread;
    private final Handler fHandler;
    private final PayloadManager fPayloadManager = new PayloadManager();
    private final Mission fMission;
    private final DJIFlightController fController;
    private final String fKey;

    private Runnable fSender;
    private int fDelay;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor for a radio link.
     *
     * @param aircraft The aircraft to communicate to
     */
    public RadioLink(DJIAircraft aircraft, Mission mission) {
        Context context = MainActivity.getInstance().getApplicationContext();

        fMission = mission;
        fController = aircraft.getFlightController();
        fKey = context.getResources().getString(R.string.pref_telemetry_freq_key);

        /* Create the thread that will handle the communication */
        fThread = new HandlerThread("radio");
        fThread.start();
        fHandler = new Handler(fThread.getLooper());

        /* Set the ready listener of the payload manager */
        fPayloadManager.setReadyListener(new PayloadManagerReadyListener());

        /* Set the payload manager in the telemetry instances */
        GPSModule.getInstance().setPayloadManager(fPayloadManager);
        SensorsModule.getInstance().setPayloadManager(fPayloadManager);

        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        /* Get the value for the frequency */
        onSharedPreferenceChanged(preferences, fKey);

        /* Add the preference listener */
        preferences.registerOnSharedPreferenceChangeListener(this);

        /* Register the callback of external data */
        fController.setReceiveExternalDeviceDataCallback(this);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Remove our listener from the payload manager */
        fPayloadManager.setReadyListener(null);

        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        /* Remove the preference listener */
        preferences.unregisterOnSharedPreferenceChangeListener(this);

        /* Remove our payload manager in the telemetry instances */
        GPSModule.getInstance().setPayloadManager(null);
        SensorsModule.getInstance().setPayloadManager(null);

        /* Join the thread */
        try {
            fThread.join();
        } catch (InterruptedException exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    @Override
    public void onResult(byte[] bytes) {
        Log.d(TAG, "Received data from onboard SDK");

        /* We only use the callback for finishing the mission */
        fMission.finishMission();
    }

    @Override
    public void onResult(DJIError error) {
        /* Check if it was an error */
        if (error == null) {
            return;
        }

        Log.e(TAG, error.getDescription());

        /* Stop sending payload */
        fSender = null;

        /* Restart the link */
        fHandler.postDelayed(new PayloadManagerRestart(), RESTART_DELAY);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        /* Make sure the frequency settings was changed */
        if (key != fKey) {
            return;
        }

        /* Update the frequency */
        double frequency = preferences.getInt(fKey, 20);
        fDelay = (int) (1000.0 / frequency);
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class is fired when the payload manager is ready to send payloads.
     */
    private class PayloadManagerReadyListener implements Runnable {
        @Override
        public void run() {
            /* Start sending payloads */
            fSender = new PayloadSender();
            fHandler.post(fSender);
        }
    }

    /**
     * This class sends a payload to the aircraft and continue the transmission.
     */
    private class PayloadSender implements Runnable {
        @Override
        public void run() {
            /* Make sure we are the current sender */
            if (fSender != this) {
                return;
            }

            /* Get the payload to send */
            IPayload payload = fPayloadManager.getNext();
            if (payload == null) {
                return;
            }

            /* Send the payload */
            byte[] bytes = payload.getPayload();
            fController.sendDataToOnboardSDKDevice(bytes, RadioLink.this);

            /* Continue the transmission later */
            fHandler.postDelayed(this, fDelay);
        }
    }

    /**
     * This class restarts the link.
     */
    private class PayloadManagerRestart implements Runnable {
        @Override
        public void run() {
            /* Request the restart to the payload manager */
            fPayloadManager.requestReady();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * This accessor returns the payload manager of the of the radio link.
     *
     * @return The payload manager of this radio link
     */
    public PayloadManager getPayloadManager() {
        return fPayloadManager;
    }

}
