package ca.polymtl.mrasl.ros;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import ca.polymtl.mrasl.tag.Tag;
import ca.polymtl.mrasl.tag.TagList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import java.util.List;

import sensor_msgs.Image;
import apriltags.Survivor;
import geometry_msgs.Point32;

/**
 * This class implements a ROS node that is subscribed to the survivor detection message.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class TagSurvivorSubscriberNode extends AbstractNodeMain {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = TagSurvivorSubscriberNode.class.getName();
    private static final String DEFAULT_NODE_NAME = "android_tag_subscriber";

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final TagList fList;

    private Subscriber<Survivor> fSubscriber;
    private MessageListener<Survivor> fListener;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public TagSurvivorSubscriberNode(TagList list) {
        fList = list;
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
        /* Create a new listener for our subscriber */
        fListener = new SurvivorListener();

        /* Create a subscriber to the survivors */
        fSubscriber = connected.newSubscriber("~/confirmed_survivors", Survivor._TYPE);
        fSubscriber.addMessageListener(fListener);
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    private class SurvivorListener implements MessageListener<Survivor> {
        @Override
        public void onNewMessage(Survivor survivor) {
            Log.d(TAG, "Received a survivor from ROS network");

            /* Crop and copy the image */
            List<Point32> corners = survivor.getDetections().getDetections().get(0).getCorners2d();
            Image image = survivor.getImage();
            Bitmap bitmap = cropBitmapFromImage(image, corners);

            /* Make an image was returned */
            if (image == null) {
                return;
            }

            /* Create the tag object */
            int ID = survivor.getDetections().getDetections().get(0).getId();
            double latitude = survivor.getLatitude();
            double longitude = survivor.getLongitude();
            Tag tag = new Tag(ID, latitude, longitude, bitmap);

            /* Add the tag to the tag list */
            fList.addTag(tag);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Util methods
    // ---------------------------------------------------------------------------------------------

    /**
     * This util method extracts the image data from a image message coming from ROS.
     *
     * @param image The ROS image message
     *
     * @return The byte representation of the image
     */
    private static byte[] getByteArrayFromImage(Image image) {
        /* Get the buffer that contains the image */
        ChannelBuffer buffer = image.getData();
        byte[] data = buffer.array();
        int offset = buffer.arrayOffset();

        /* Copy the image from the buffer */
        byte[] img = new byte[data.length - offset];
        for (int i = 0; i < img.length; i++) {
            img[i] = data[offset + i];
        }

        return img;
    }

    /**
     * This util method crops a ROS image into a bitmap. It assumes that the image format that we
     * receive is RGB888. It converts the image into a ARGB8888.
     *
     * @param image   The ROS image message
     * @param corners The points used for cropping
     *
     * @return The cropped image
     */
    private static Bitmap cropBitmapFromImage(Image image, List<Point32> corners) {
        /* Get the data from the image */
        byte[] buffer = getByteArrayFromImage(image);
        int w1 = image.getWidth();
        int h1 = image.getHeight();

        /* The image cannot be empty */
        if (w1 == 0 || h1 == 0) {
            return null;
        }

        /* We need 4 points to crop the image */
        if (corners.size() != 4) {
            Log.e(TAG, "The received image doesn't have 4 corners.");
            return null;
        }

        /* Find the lowest and highest points for cropping */
        Point lowest = new Point(h1, w1);
        Point highest = new Point(0, 0);
        for (Point32 point : corners) {
            int x = (int) point.getX();
            int y = (int) point.getY();

            /* Find the min/max x component */
            if (x < lowest.x) {
                lowest.x = x;
            } else if (x > highest.x) {
                highest.x = x;
            }

            /* Find the min/max y component */
            if (y < lowest.y) {
                lowest.y = y;
            } else if (y > highest.y) {
                highest.y = y;
            }
        }

        /* Allocate the pixel data */
        int w2 = highest.x - lowest.x;
        int h2 = highest.y - lowest.y;
        int bitmapSize = w2 * h2;
        int bitmap[] = new int[bitmapSize];

        /* Convert and crop the image from RGB888 to ARGB8888 */
        int start = w1 * lowest.y + lowest.x;
        int end = w1 * highest.y + highest.x;
        for (int i = start, j = 0; i < end; i++) {
            int currentX = i % w1;
            int currentY = i / w1;

            /* Make sure that the pixel is in the cropping region */
            if (currentX < lowest.x || currentX >= highest.x) {
                continue;
            }

            /* Make sure that the pixel is in the cropping region */
            if (currentY < lowest.y || currentY >= highest.y) {
                continue;
            }

            /* Convert the pixel into the right format */
            bitmap[j] = 0xFF << 24;
            bitmap[j] += buffer[3 * i + 0] << 16;
            bitmap[j] += buffer[3 * i + 1] << 8;
            bitmap[j] += buffer[3 * i + 2];
            j++;
        }

        /* Create the bitmap */
        return Bitmap.createBitmap(bitmap, w2, h2, Bitmap.Config.ARGB_8888);
    }

}
