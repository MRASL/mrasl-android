package ca.polymtl.mrasl.ros;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ca.polymtl.mrasl.ui.activity.MainActivity;
import ca.polymtl.mrasl.R;

/**
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class RosConnection implements OnSharedPreferenceChangeListener {

    private static RosConnection Instance;

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = RosConnection.class.getName();

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final HandlerThread fThread;
    private final Handler fHandler;
    private final String fKeyAddress;
    private final String fKeyPort;
    private final List<NodeMain> fNodes = new ArrayList<>();

    private NodeMainExecutor fExecutor;
    private URI fMasterURI;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * This method returns the instance to the ROS connection. If the instance hasn't been created,
     * it will create one without a URI configured.
     *
     * @return The instance of the ROS connection
     */
    public static RosConnection getInstance() {
        if (Instance == null) {
            Instance = new RosConnection();
        }

        return Instance;
    }

    RosConnection() {
        /* Get the preferences keys */
        Context context = MainActivity.getInstance().getApplicationContext();
        fKeyAddress = context.getResources().getString(R.string.pref_connection_address_key);
        fKeyPort = context.getResources().getString(R.string.pref_connection_port_key);

        fThread = new HandlerThread("ros");
        fThread.start();
        fHandler = new Handler(fThread.getLooper());

        /* Generate a default URI */
        fMasterURI = getURIFromPreferences();
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method generates the master URI configured by the user.
     *
     * @return The master URI configured by the user
     */
    private URI getURIFromPreferences() {
        /* Get the shared preferences */
        MainActivity main = MainActivity.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);

        /* Get the address and the port of the master node */
        String addr = preferences.getString(fKeyAddress, "127.0.0.1");
        int port = preferences.getInt(fKeyPort, 5000);

        /* Create the new URI */
        return URI.create("http://" + String.valueOf(addr) + ':' + String.valueOf(port) + '/');
    }

    /**
     * This method launches a ROS node and connects it to the master node.
     *
     * @param node The node to launch
     */
    public void launchNode(NodeMain node) {
        fHandler.post(new LaunchNode(node));
    }

    /**
     * This method shutdown all the nodes. It must be called before exiting the application or the
     * master node will have zombie nodes.
     */
    public void shutdown() {
        fHandler.post(new Shutdown());
    }

    /**
     * This method shutdown an individual node.
     *
     * @param node The node to shutdown
     */
    public void shutdownNode(NodeMain node) {
        fHandler.post(new ShutdownNode(node));
    }

    /**
     * This method restarts all the node launched by this launcher. It should be used when the port
     * or the address to the master node is changed.
     */
    public void restart() {
        fHandler.post(new Restart());
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        /* Check that address or the port was the preferences that was changed */
        if (key != fKeyAddress && key != fKeyPort) {
            return;
        }

        /* Create the URI to the master node again */
        fMasterURI = getURIFromPreferences();

        /* Restart all the node using the new address */
        restart();
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    private class LaunchNode implements Runnable {
        private final NodeMain fNode;

        public LaunchNode(NodeMain node) {
            fNode = node;
        }

        @Override
        public void run() {
            if (fExecutor == null) {
                fExecutor = DefaultNodeMainExecutor.newDefault();
            }

            /* Create the configuration to connect to the master node */
            NodeConfiguration config = NodeConfiguration.newPrivate(fMasterURI);

            /* Start the node */
            fExecutor.execute(fNode, config);

            /* Keep a reference to the node */
            if (!fNodes.contains(fNode)) {
                fNodes.add(fNode);
            }
        }
    }

    private class Shutdown implements Runnable {
        @Override
        public void run() {
            /* Shutdown all the node */
            fExecutor.shutdown();
            fExecutor = null;

            /* Clear the node list */
            fNodes.clear();
        }
    }

    private class ShutdownNode implements Runnable {
        private final NodeMain fNode;

        public ShutdownNode(NodeMain node) {
            fNode = node;
        }

        @Override
        public void run() {
            /* Make sure our node is in the list */
            if (!fNodes.contains(fNode)) {
                return;
            }

            /* Shutdown the node */
            fExecutor.shutdownNodeMain(fNode);

            /* Remove the node from the list */
            fNodes.remove(fNode);
        }
    }

    public class Restart implements Runnable {
        @Override
        public void run() {
            /* Shutdown all the node */
            fExecutor.shutdown();
            fExecutor = null;

            /* Restart all the node */
            for (NodeMain node : fNodes) {
                launchNode(node);
            }
        }
    }

}
