package ca.polymtl.mrasl.ros;

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import apriltags.Survivor;
import ca.polymtl.mrasl.mission.Mission;

/**
 * This class creates a ROS node that is subscribed to the mission planner states.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class MissionPlannerSubscriberNode extends AbstractNodeMain implements MessageListener<Survivor> {

    /**
     * TODO: use the proper ROS message
     */

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = MissionPlannerSubscriberNode.class.getName();
    private static final String DEFAULT_NODE_NAME = "android_mission_planner_subscriber";

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Mission fMission;

    private Subscriber<Survivor> fSubscriber;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param mission The mission to send the states
     */
    public MissionPlannerSubscriberNode(Mission mission) {
        fMission = mission;
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(DEFAULT_NODE_NAME);
    }

    @Override
    public void onStart(ConnectedNode connected) {
        fSubscriber = connected.newSubscriber("~/state", Survivor._TYPE);
        fSubscriber.addMessageListener(this);
    }

    @Override
    public void onNewMessage(Survivor survivor) {
        Log.d(TAG, "Received message");
    }

}
