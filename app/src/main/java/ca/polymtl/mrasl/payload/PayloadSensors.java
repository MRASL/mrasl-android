package ca.polymtl.mrasl.payload;

import static ca.polymtl.mrasl.shared.PayloadUtil.putFloatToBytes;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Arrays;

/**
 * This class implements a payload for the sensors module.
 * <p>
 * [  0 -  3 ] The X acceleration in float
 * [  4 -  7 ] The Y acceleration in float
 * [  8 - 11 ] The Z acceleration in float
 * [ 12 - 15 ] The x*sin(θ/2) rotation in float
 * [ 16 - 19 ] The y*sin(θ/2) rotation in float
 * [ 20 - 23 ] The z*sin(θ/2) rotation in float
 * [ 24 - 27 ] The cos(θ/2) rotation in float
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PayloadSensors implements IPayload {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int PAYLOAD_SIZE = 28;
    private static final int POS_ACC_X = 0;
    private static final int POS_ACC_Y = 4;
    private static final int POS_ACC_Z = 8;
    private static final int POS_ROT_1 = 12;
    private static final int POS_ROT_2 = 16;
    private static final int POS_ROT_3 = 20;
    private static final int POS_ROT_4 = 24;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private float[] fAccelerometer = {0L, 0L, 0L};
    private float[] fRotation = {0L, 0L, 0L, 0L};

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public PayloadSensors(SensorEvent event) {
        /* Compute the fields */
        computeFields(event);
    }

    public PayloadSensors(SensorEvent event, PayloadSensors payload) {
        /* Copy the sensors data from the previous packet */
        fAccelerometer = Arrays.copyOf(payload.fAccelerometer, payload.fAccelerometer.length);
        fRotation = Arrays.copyOf(payload.fRotation, payload.fRotation.length);

        /* Compute the fields */
        computeFields(event);
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    private void computeFields(SensorEvent event) {
        /* Check which sensor triggered the listener */
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                fAccelerometer = event.values;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                fRotation = event.values;
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[PAYLOAD_SIZE];

        /* Format the payload */
        putFloatToBytes(payload, POS_ACC_X, fAccelerometer[0]);
        putFloatToBytes(payload, POS_ACC_Y, fAccelerometer[1]);
        putFloatToBytes(payload, POS_ACC_Z, fAccelerometer[2]);
        putFloatToBytes(payload, POS_ROT_1, fRotation[0]);
        putFloatToBytes(payload, POS_ROT_2, fRotation[1]);
        putFloatToBytes(payload, POS_ROT_3, fRotation[2]);
        putFloatToBytes(payload, POS_ROT_4, fRotation[3]);

        return payload;
    }


    @Override
    public String toString() {
        return "Accelerometer: " +
                fAccelerometer[0] + " " +
                fAccelerometer[1] + " " +
                fAccelerometer[2] +
                "\nRotation: " +
                fRotation[0] + " " +
                fRotation[1] + " " +
                fRotation[2] + " " +
                fRotation[3];
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessor that returns the acceleration in the XYZ axes.
     *
     * @return The acceleration of the device
     */
    public float[] getAccelerometer() {
        return fAccelerometer;
    }

    /**
     * Accessor that returns the rotation values.
     *
     * @return The rotation of the device
     */
    public float[] getRotation() {
        return fRotation;
    }

}
