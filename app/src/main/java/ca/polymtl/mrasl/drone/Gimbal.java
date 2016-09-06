package ca.polymtl.mrasl.drone;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static dji.sdk.Gimbal.DJIGimbal.DJIGimbalWorkMode;
import static dji.sdk.base.DJIBaseComponent.DJICompletionCallback;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.ui.activity.MainActivity;
import dji.sdk.Gimbal.DJIGimbal;
import dji.sdk.base.DJIError;

/**
 * This class is a wrapper to the {@link DJIGimbal}. Right now, it only changes the work mode of the
 * gimbal based on the preferences.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Gimbal implements
        IDisposable,
        OnSharedPreferenceChangeListener,
        DJICompletionCallback {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = Gimbal.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final DJIGimbal fGimbal;
    private final String fKey;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Factory constructor for wrapping a gimbal. It will return {@code null} if the provided gimbal
     * is {@code null}.
     *
     * @param gimbal The gimbal to control
     *
     * @return The new gimbal object
     */
    public static Gimbal from(DJIGimbal gimbal) {
        /* Make sure the gimbal isn't null */
        if (gimbal == null) {
            return null;
        }

        /* Create the new gimbal */
        return new Gimbal(gimbal);
    }

    /**
     * Constructor.
     *
     * @param gimbal A non {@code null} gimbal
     */
    Gimbal(DJIGimbal gimbal) {
        Context context = MainActivity.getInstance().getApplicationContext();

        fGimbal = gimbal;
        fKey = context.getResources().getString(R.string.pref_gimbal_mode_key);

        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        /* Register a listener when the mode is changed by the user */
        preferences.registerOnSharedPreferenceChangeListener(this);

        /* Set the gimbal work mode */
        onSharedPreferenceChanged(preferences, fKey);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        /* Unregister the listener */
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        /* Make sure the work mode was changed */
        if (key != fKey) {
            return;
        }

        /* Get the new value */
        int value = Integer.parseInt(preferences.getString(fKey, "0"));

        /* Find the work mode tied to the value */
        DJIGimbalWorkMode mode;
        switch (value) {
            case 0:
                mode = DJIGimbalWorkMode.FreeMode;
                break;
            case 1:
                mode = DJIGimbalWorkMode.FpvMode;
                break;
            case 2:
                mode = DJIGimbalWorkMode.YawFollowMode;
                break;
            case 255:
            default:
                mode = DJIGimbalWorkMode.Unknown;
                break;
        }

        /* Change the gimbal work mode */
        fGimbal.setGimbalWorkMode(mode, this);
    }

    @Override
    public void onResult(DJIError error) {
        /* Check if it was a success */
        if (error == null) {
            return;
        }

        Log.e(TAG, error.getDescription());
    }

}
