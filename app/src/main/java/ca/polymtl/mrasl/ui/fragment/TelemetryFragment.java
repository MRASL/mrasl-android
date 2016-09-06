package ca.polymtl.mrasl.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.payload.PayloadGPS;
import ca.polymtl.mrasl.payload.PayloadSensors;
import ca.polymtl.mrasl.telemetry.GPSModule;
import ca.polymtl.mrasl.telemetry.SensorsModule;

/**
 * This class shows the sensors and the GPS information.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class TelemetryFragment extends Fragment {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int INTERVAL = 1000;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Handler fHandler = new Handler();

    private UpdateInfoTimer fUpdateInfo;

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.content_telemetry, container, false);

        /* Start the update timer */
        fUpdateInfo = new UpdateInfoTimer(view);
        fUpdateInfo.start();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* Stop the update timer */
        fUpdateInfo.stop();
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class update information about the telemetry in this activity.
     */
    private class UpdateInfoTimer implements Runnable {
        /* GPS information */
        private final TextView fStatus;
        private final TextView fLatitude;
        private final TextView fLongitude;
        private final TextView fAltitude;
        private final TextView fAccuracy;
        private final TextView fSpeed;

        /* Sensors information */
        private final TextView fAccelerationX;
        private final TextView fAccelerationY;
        private final TextView fAccelerationZ;
        private final TextView fRotation1;
        private final TextView fRotation2;
        private final TextView fRotation3;
        private final TextView fRotation4;
        private final DecimalFormat fFormatter = new DecimalFormat("0.000000000");

        private UpdateInfoTimer(View view) {
            /* Get the GPS text views */
            fStatus = (TextView) view.findViewById(R.id.gps_status);
            fLatitude = (TextView) view.findViewById(R.id.gps_latitude);
            fLongitude = (TextView) view.findViewById(R.id.gps_longitude);
            fAltitude = (TextView) view.findViewById(R.id.gps_altitude);
            fAccuracy = (TextView) view.findViewById(R.id.gps_accuracy);
            fSpeed = (TextView) view.findViewById(R.id.gps_speed);

            /* Get the sensors text views */
            fAccelerationX = (TextView) view.findViewById(R.id.sensors_acc_x);
            fAccelerationY = (TextView) view.findViewById(R.id.sensors_acc_y);
            fAccelerationZ = (TextView) view.findViewById(R.id.sensors_acc_z);
            fRotation1 = (TextView) view.findViewById(R.id.sensors_rotation_1);
            fRotation2 = (TextView) view.findViewById(R.id.sensors_rotation_2);
            fRotation3 = (TextView) view.findViewById(R.id.sensors_rotation_3);
            fRotation4 = (TextView) view.findViewById(R.id.sensors_rotation_4);
        }

        public void start() {
            run();
        }

        public void stop() {
            fHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            /* Update the text fields */
            updateGPS();
            updateSensors();

            /* Set the timer for running the task later */
            fHandler.postDelayed(this, INTERVAL);
        }

        public void updateGPS() {
            /* Get the GPS module instance */
            GPSModule module = GPSModule.getInstance();


            /* Check if we are still connected */
            if (module.isConnected()) {
                fStatus.setText("Connected");
            } else {
                fStatus.setText("Disconnected");
            }

            /* Make sure there is a payload available */
            PayloadGPS payload = module.getPayload();
            if (payload == null) {
                return;
            }

            /* Update the text views of the GPS information */
            fLatitude.setText(String.valueOf(payload.getLatitude()));
            fLongitude.setText(String.valueOf(payload.getLongitude()));
            fAltitude.setText(String.valueOf(payload.getAltitude()));
            fAccuracy.setText(String.valueOf(payload.getAccuracy()));
            fSpeed.setText(String.valueOf(payload.getSpeed()));
        }

        public void updateSensors() {
            /* Make sure there is a payload available */
            PayloadSensors payload = SensorsModule.getInstance().getPayload();
            if (payload == null) {
                return;
            }

            /* Update the text views of the sensors information */
            fAccelerationX.setText(fFormatter.format(payload.getAccelerometer()[0]));
            fAccelerationY.setText(fFormatter.format(payload.getAccelerometer()[1]));
            fAccelerationZ.setText(fFormatter.format(payload.getAccelerometer()[2]));
            fRotation1.setText(fFormatter.format(payload.getRotation()[0]));
            fRotation2.setText(fFormatter.format(payload.getRotation()[1]));
            fRotation3.setText(fFormatter.format(payload.getRotation()[2]));
            fRotation4.setText(fFormatter.format(payload.getRotation()[3]));
        }
    }

}
