package ca.polymtl.mrasl.telemetry;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.payload.PayloadManager;
import ca.polymtl.mrasl.payload.PayloadSensors;

/**
 * This class implements a telemetry module for the sensors system.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SensorsModule implements ITelemetryModule<PayloadSensors> {

    private static SensorsModule Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int FREQUENCY = SensorManager.SENSOR_DELAY_FASTEST;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Context fContext;
    private final HandlerThread fThread;
    private final Handler fHandler;
    private final SensorManager fSensorManager;
    private final Sensor fAccelerometer;
    private final Sensor fRotation;

    private PayloadManager fPayloadManager;
    private PayloadSensors fPayload;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public static SensorsModule getInstance() {
        /* Setup the singleton instance */
        if (Instance == null) {
            Instance = new SensorsModule();
        }

        return Instance;
    }

    SensorsModule() {
        fContext = MainActivity.getInstance().getApplicationContext();

        /* Get the sensor service */
        fSensorManager = (SensorManager) fContext.getSystemService(fContext.SENSOR_SERVICE);

        /* Get accelerometer and magnetic sensor */
        fAccelerometer = fSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        fRotation = fSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        /* Start the thread that handles the sensor events */
        fThread = new HandlerThread("sensors");
        fThread.start();
        fHandler = new Handler(fThread.getLooper());

        /* Add listener to the sensor */
        SensorEventListener listener = new SensorsListener();
        fSensorManager.registerListener(listener, fAccelerometer, FREQUENCY, fHandler);
        fSensorManager.registerListener(listener, fRotation, FREQUENCY, fHandler);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public PayloadSensors getPayload() {
        return fPayload;
    }

    @Override
    public void setPayloadManager(PayloadManager manager) {
        fPayloadManager = manager;
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * Class that listens to the sensors updates.
     */
    private class SensorsListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            /* We copy the information about the previous sensors, if possible */
            if (fPayload == null) {
                fPayload = new PayloadSensors(event);
            } else {
                fPayload = new PayloadSensors(event, fPayload);
            }

            /* Add the payload to the queue if possible */
            if (fPayloadManager != null) {
                fPayloadManager.setPayload(fPayload);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

}
