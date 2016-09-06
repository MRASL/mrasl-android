package ca.polymtl.mrasl.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.drone.Aircraft;
import ca.polymtl.mrasl.drone.Camera;
import ca.polymtl.mrasl.drone.Registration;

public class CameraFragment extends Fragment {

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Runnable fConnectionListener = new ConnectionChangeListener();

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.content_camera, container, false);

        /* Add the listener that rotates the video feed */
        TextureView texture = (TextureView) view.findViewById(R.id.camera_view);
        texture.post(new RotateVideoFrame());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* Remove our listener for the connection change */
        Registration.getInstance().removeConnectionListener(fConnectionListener);
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    private class RotateVideoFrame implements Runnable {
        @Override
        public void run() {
            FrameLayout layout = (FrameLayout) getView().findViewById(R.id.camera_view_layout);
            int w = layout.getWidth();
            int h = layout.getHeight();

            Log.d("Camera", String.valueOf(w) + " " + String.valueOf(h));
            layout.setRotation(90);

            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.width = w;
            params.height = h;
            layout.setLayoutParams(params);

            /* Add a listener for a connection change */
            Registration.getInstance().addConnectionListener(fConnectionListener);
        }
    }

    private class ConnectionChangeListener implements Runnable {
        private void connect() {
            /* Make sure the view is loaded */
            View view = getView();
            if (view == null) {
                return;
            }

            /* Make sure that an aircraft is connected */
            Aircraft aircraft = Registration.getInstance().getAicraft();
            if (aircraft == null) {
                return;
            }

            /* Make sure that the aircraft has a camera */
            Camera camera = aircraft.getCamera();
            if (camera == null) {
                return;
            }

            /* Setup our texture to receive the video feed */
            TextureView texture = (TextureView) view.findViewById(R.id.camera_view);
            camera.setVideoSurface(texture);
        }

        @Override
        public void run() {
            connect();
        }
    }

}
