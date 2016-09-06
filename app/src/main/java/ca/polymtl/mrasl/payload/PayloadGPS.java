package ca.polymtl.mrasl.payload;

import static ca.polymtl.mrasl.shared.PayloadUtil.putFloatToBytes;
import static ca.polymtl.mrasl.shared.PayloadUtil.putDoubleToBytes;

import android.location.Location;

/**
 * This class implements a payload for the GPS module. The format of the payload is defined in the
 * following graph:
 * <p/>
 * [  0 -  3 ] The latitude in float
 * [  4 -  7 ] The longitude in float
 * [  8 - 11 ] The altitude in float
 * [ 12 - 15 ] The bearing in float
 * [ 16 - 19 ] The accuracy in float
 * [ 20 - 23 ] The speed in float
 * [ 24      ] A null byte
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PayloadGPS implements IPayload {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int PAYLOAD_SIZE = 25;
    private static final int POS_LATITUDE = 0;
    private static final int POS_LONGITUDE = 4;
    private static final int POS_ALTITUDE = 8;
    private static final int POS_BEARING = 12;
    private static final int POS_ACCURACY = 16;
    private static final int POS_SPEED = 20;
    private static final int POS_NULL = 24;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final float fLatitude;
    private final float fLongitude;
    private final float fAltitude;
    private final float fBearing;
    private final float fAccuracy;
    private final float fSpeed;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public PayloadGPS() {
        fLatitude = 0L;
        fLongitude = 0L;
        fAltitude = 0L;
        fBearing = 0L;
        fAccuracy = 0L;
        fSpeed = 0L;
    }

    /**
     * Constructor with a {@link Location} provided.
     *
     * @param location The location to build the payload from
     */
    public PayloadGPS(Location location) {
        /* The GPSModule always provides the latitude and longitude */
        fLatitude = (float) location.getLatitude();
        fLongitude = (float) location.getLongitude();

        /* Set the altitude if possible */
        if (location.hasAltitude()) {
            fAltitude = (float) location.getAltitude();
        } else {
            fAltitude = 0L;
        }

        /* Set the bearing if possible */
        if (location.hasBearing()) {
            fBearing = location.getBearing();
        } else {
            fBearing = 0L;
        }

        /* Set the accuracy if possible */
        if (location.hasAccuracy()) {
            fAccuracy = location.getAccuracy();
        } else {
            fAccuracy = 0L;
        }

        /* Set the speed if possible */
        if (location.hasSpeed()) {
            fSpeed = location.getSpeed();
        } else {
            fSpeed = 0L;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[PAYLOAD_SIZE];

        /* Format the payload */
        putFloatToBytes(payload, POS_LATITUDE, fLatitude);
        putFloatToBytes(payload, POS_LONGITUDE, fLongitude);
        putFloatToBytes(payload, POS_ALTITUDE, fAltitude);
        putFloatToBytes(payload, POS_BEARING, fBearing);
        putFloatToBytes(payload, POS_ACCURACY, fAccuracy);
        putFloatToBytes(payload, POS_SPEED, fSpeed);
        payload[POS_NULL] = (byte) 0x0;

        return payload;
    }

    @Override
    public String toString() {
        return "Latitude: " + getLatitude() +
                "\nLongitude: " + getLongitude() +
                "\nAltitude: " + getAltitude() +
                "\nBearing: " + getBearing() +
                "\nAccuracy: " + getAccuracy() +
                "\nSpeed: " + getSpeed();
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessor that returns the latitude in the payload.
     *
     * @return The latitude in the payload
     */
    public float getLatitude() {
        return fLatitude;
    }

    /**
     * Accessor that returns the longitude in the payload.
     *
     * @return The longitude in the payload
     */
    public float getLongitude() {
        return fLongitude;
    }

    /**
     * Accessor that returns the altitude in the payload.
     *
     * @return The altitude in the payload
     */
    public float getAltitude() {
        return fAltitude;
    }

    /**
     * Accessor that returns the bearing in the payload.
     *
     * @return The bearing in the payload
     */
    public float getBearing() {
        return fBearing;
    }

    /**
     * Accessor that returns the accuracy of the position in the payload.
     *
     * @return The accuracy in the payload
     */
    public float getAccuracy() {
        return fAccuracy;
    }

    /**
     * Accessor that returns the current speed in the payload.
     *
     * @return The current speed in the payload
     */
    public float getSpeed() {
        return fSpeed;
    }

}
