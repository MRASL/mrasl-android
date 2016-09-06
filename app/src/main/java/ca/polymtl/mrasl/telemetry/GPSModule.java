package ca.polymtl.mrasl.telemetry;

import static android.location.GpsStatus.Listener;

import android.Manifest;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.payload.PayloadGPS;
import ca.polymtl.mrasl.payload.PayloadManager;

/**
 * This class implements a telemetry module for the GPS system.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class GPSModule implements ITelemetryModule<PayloadGPS> {

    private static GPSModule Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String PERM_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Context fContext;
    private final LocationManager fLocManager;

    private PayloadManager fPayloadManager;
    private PayloadGPS fPayload;
    private boolean fConnected;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public static GPSModule getInstance() {
        /* Setup the singleton instance */
        if (Instance == null) {
            Instance = new GPSModule();
        }

        return Instance;
    }

    GPSModule() {
        fContext = MainActivity.getInstance().getApplicationContext();

        /* Get the GPS service */
        fLocManager = (LocationManager) fContext.getSystemService(Context.LOCATION_SERVICE);

        /* Request location updates into our listener */
        LocationListener listener = new LocListener();

        /* Request as location updates */
        fLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        fLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

        /* Add a GPS status listener */
        fLocManager.addGpsStatusListener(new StatusListener());

        /* Create a default GPS payload */
        fPayload = new PayloadGPS();

        /* We implied we are already connected */
        fConnected = true;
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public PayloadGPS getPayload() {
        return fPayload;
    }

    @Override
    public void setPayloadManager(PayloadManager manager) {
        fPayloadManager = manager;
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * This accessor returns whether we are connected to GPS or not.
     *
     * @return {@code true} if we are connected, else {@code false}.
     */
    public boolean isConnected() {
        return fConnected;
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * Class that listens to the location updates.
     */
    private class LocListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            fPayload = new PayloadGPS(location);

            /* Add the payload to the queue if possible */
            if (fPayloadManager != null) {
                fPayloadManager.setPayload(fPayload);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    /**
     * Class that listens to the GPS status updates.
     */
    private class StatusListener implements Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    fConnected = true;
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    fConnected = false;
                    break;
            }
        }
    }

}
