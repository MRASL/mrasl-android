package ca.polymtl.mrasl.drone;

import static dji.sdk.Camera.DJICameraSettingsDef.CameraMode;
import static dji.sdk.Camera.DJICamera.CameraReceivedVideoDataCallback;
import static dji.sdk.base.DJIBaseComponent.DJICompletionCallback;
import static dji.sdk.base.DJIBaseComponent.DJICompletionCallbackWith;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.TextureView;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.mission.MissionStateChangedListener;
import ca.polymtl.mrasl.mission.State;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.ui.activity.MainActivity;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;

/**
 * This class represents the camera of an aircraft.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Camera implements
        IDisposable,
        MissionStateChangedListener,
        DJICompletionCallback {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = Camera.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final DJICamera fCamera;
    private final Mission fMission;
    private final String fKey;

    private DJICodecManager fCodec;
    private TextureView fVideoTextureView;
    private SurfaceTexture fVideoSurfaceTexture;
    private boolean fRecording;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Factory constructor for a camera. It makes sure that the {@link DJIBaseProduct} contains a
     * {@link DJICamera}. It returns {@code null} if it does not.
     *
     * @param product The product to create a camera from
     * @param mission The mission to receive state
     *
     * @return The camera of the product
     */
    public static Camera createCamera(DJIBaseProduct product, Mission mission) {
        /* Make sure that the product has a camera */
        DJICamera camera = product.getCamera();
        if (camera == null) {
            return null;
        }

        return new Camera(camera, mission);
    }

    /**
     * Constructor for a camera.
     *
     * @param camera  The camera of a {@link DJIBaseProduct}
     * @param mission The mission to receive state
     */
    Camera(DJICamera camera, Mission mission) {
        Context context = MainActivity.getInstance().getApplicationContext();

        fCamera = camera;
        fMission = mission;
        fKey = context.getResources().getString(R.string.pref_camera_rec_key);

        /* Remove the listener for state changes */
        fMission.addMissionStateChangedListener(this);
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method returns whether we are supposed to record the video or not to the SD card of the
     * drone.
     *
     * @return {@code true} if recording is enabled, else {@code false}
     */
    private boolean isRecordingEnabled() {
        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        Log.d(TAG, String.valueOf(preferences.getBoolean(fKey, false)));

        /* Get the preference value */
        return preferences.getBoolean(fKey, false);
    }

    /**
     * This method starts the recording of the mission.
     */
    private void startRecording() {
        /* Make sure recording is enabled */
        if (!isRecordingEnabled()) {
            return;
        }

        /* Make sure we are not already recording */
        if (fRecording) {
            return;
        }

        /**
         * We try to start the video recording using a chain of asynchronous callbacks:
         * <p>
         * 1. We check if the camera mode is set to recording. If it is not, we try to change the
         *    mode.
         * 2. If the mode has been correctly set, we can request to start the recording.
         * 3. If the recording was started, we set the flag.
         * <p>
         * @see CheckCameraMode
         * @see CameraModeChanged
         * @see StartRecording
         */
        fCamera.getCameraMode(new CheckCameraMode());
    }

    /**
     * This method stops the recording of the mission.
     */
    private void stopRecording() {
        /* Make sure we are recording */
        if (!fRecording) {
            return;
        }

        /* Stop the recording */
        fCamera.stopRecordVideo(this);
        fRecording = false;
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Remove the listener */
        fMission.removeMissionStateChangedListener(this);
    }

    @Override
    public void onStateChanged(State state) {
        switch (state) {
            case START_MISSION:
                startRecording();
                break;
            case ABORT_LANDING:
            case ABORT_MISSION:
                stopRecording();
                break;
        }
    }

    @Override
    public void onResult(DJIError error) {
        if (error == null) {
            return;
        }

        Log.d(TAG, error.getDescription());
    }

    // ---------------------------------------------------------------------------------------------
    // Mutators
    // ---------------------------------------------------------------------------------------------

    public void setVideoSurface(TextureView surface) {
        fVideoTextureView = surface;

        /* Check if can create the decoder right away */
        if (surface.isAvailable()) {
            /* Get surface metadata */
            fVideoSurfaceTexture = fVideoTextureView.getSurfaceTexture();
            int w = fVideoTextureView.getWidth();
            int h = fVideoTextureView.getHeight();

            /* Create the decoder */
            Context context = MainActivity.getInstance().getApplicationContext();
            fCodec = new DJICodecManager(context, fVideoSurfaceTexture, w, h);
        } else {
            fCodec = null;
            fVideoSurfaceTexture = null;
        }

        /* Set the surface listener */
        surface.setSurfaceTextureListener(new TextureListener());

        /* Set the callback for receiving frames */
        fCamera.setDJICameraReceivedVideoDataCallback(new CameraCallback());
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class receives the callback that checks the camera mode. If will start the recording if
     * the camera is in recording mode. It will set the camera mode to recording if it is not.
     */
    private class CheckCameraMode implements DJICompletionCallbackWith<CameraMode> {
        @Override
        public void onSuccess(CameraMode mode) {
            if (mode == CameraMode.RecordVideo) {
                fCamera.startRecordVideo(new StartRecording());
            } else {
                fCamera.setCameraMode(CameraMode.RecordVideo, new CameraModeChanged());
            }
        }

        @Override
        public void onFailure(DJIError error) {
            onResult(error);
        }
    }

    /**
     * This class receives the callback that the camera mode was changed to recording. If no error
     * happened, we can start the recording.
     */
    private class CameraModeChanged implements DJICompletionCallback {
        @Override
        public void onResult(DJIError error) {
            /* Check if the mode was changed */
            if (error != null) {
                Camera.this.onResult(error);
                return;
            }

            /* Make sure recording is enabled */
            if (!isRecordingEnabled()) {
                return;
            }

            /* Start recording */
            fCamera.startRecordVideo(new StartRecording());
        }
    }

    /**
     * This class receives the callback that the recording was started.
     */
    private class StartRecording implements DJICompletionCallback {
        @Override
        public void onResult(DJIError error) {
            /* Check if the recording was started */
            if (error != null) {
                Camera.this.onResult(error);
                return;
            }

            /* Update the flag */
            fRecording = true;
        }
    }

    /**
     * This class listens for events related to the texture that will show the camera feed.
     */
    private class TextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int w, int h) {
            /* Create a decoder if there is none */
            if (fCodec == null) {
                Context context = MainActivity.getInstance().getApplicationContext();
                fCodec = new DJICodecManager(context, surface, w, h);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            /* Remove the codec if any */
            if (fCodec != null) {
                fCodec.cleanSurface();
                fCodec = null;
            }

            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    /**
     * This class handles the callback when a frame is ready for decoding.
     */
    private class CameraCallback implements CameraReceivedVideoDataCallback {
        @Override
        public void onResult(byte[] frame, int size) {
            /* Make sure we have a decoder */
            if (fCodec == null) {
                return;
            }

            /* Send the frame for decoding */
            fCodec.sendDataToDecoder(frame, size);
        }
    }

}
