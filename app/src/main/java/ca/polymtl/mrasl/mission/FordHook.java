package ca.polymtl.mrasl.mission;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ford.DJIL;
import com.ford.DJILListener;
import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.ui.activity.MainActivity;

/**
 * This class handles the event coming from the FordHook SDK.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class FordHook implements IDisposable, DJILListener {

    private static FordHook Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = FordHook.class.getName();
    private static final int DELAY_RECONNECT = 1000;
    private static final int DELAY_MESSAGE = 60000;
    private static String TEAM = "Team MRASL";
    private static String[] MESSAGES = {
            "Don't panic.",
            "The pizza is coming.",
            "The cake is a lie.",
            "Just eat!",
    };

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final HandlerThread fThread;
    private final Handler fHandler;
    private final Runnable fConnection = new Connection();
    private final Runnable fSendMessage = new SendRandomMessage();
    private final DJIL fLink;
    private final Mission fMission;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public FordHook(Mission mission) {
        fMission = mission;
        fLink = new DJIL(this);

        /* Start the thread that handles the ford connection */
        fThread = new HandlerThread("ford");
        fThread.start();

        /* Start the connection */
        fHandler = new Handler(fThread.getLooper());
        fHandler.post(fConnection);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Remove periodic callbacks */
        fHandler.removeCallbacks(fConnection);
        fHandler.removeCallbacks(fSendMessage);

        /* Disconnect from the server */
        fLink.disconnect();
    }

    @Override
    public void startButton() {
        Log.d(TAG, "Start mission request from Ford hook");

        /* Execute the command */
        fMission.startMission();
    }

    @Override
    public void abortLanding() {
        Log.d(TAG, "Abort landing request from Ford hook");

        /* Execute the command */
        fMission.abortLanding();
    }

    @Override
    public void abortMission() {
        Log.d(TAG, "Abort mission request from Ford hook");

        /* Execute the command */
        fMission.abortMission();
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class connects to the FordHook server.
     */
    private class Connection implements Runnable {
        @Override
        public void run() {
            /* Get the shared preferences */
            MainActivity main = MainActivity.getInstance();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

            /* Get the frequency from the preferences */
            String address = preferences.getString("ford_address", "127.0.0.1");

            Log.d(TAG, "Trying connection to Ford SDK on " + address);
            try {
                /* Try to connect */
                fLink.connect(address, TEAM);

                Log.d(TAG, "Successfully connected to Ford SDK");

                /* Send a random message */
                fHandler.post(fSendMessage);
            } catch (Exception exception) {
                /* Retry the connection later */
                fHandler.postDelayed(fConnection, DELAY_RECONNECT);
                exception.printStackTrace();
            }
        }
    }

    /**
     * This class sends random messages to the Ford screen.
     */
    private class SendRandomMessage implements Runnable {
        private Iterator<String> fMessage;

        public SendRandomMessage() {
            /* Shuffle the list */
            List<String> messages = Arrays.asList(MESSAGES);
            Collections.shuffle(messages);

            /* Create a circular iterator */
            fMessage = Iterators.cycle(messages);
        }

        @Override
        public void run() {
            /* Send the message  */
            if(fMessage.hasNext()) {
                fLink.show(fMessage.next());
            }

            /* Send another message later */
            fHandler.postDelayed(this, DELAY_MESSAGE);
        }
    }

}
