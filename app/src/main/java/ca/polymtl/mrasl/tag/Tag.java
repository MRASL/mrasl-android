package ca.polymtl.mrasl.tag;

import android.graphics.Bitmap;

import sensor_msgs.Image;

/**
 * This class contains a tag that has been found.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Tag {

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final int fID;
    private final double fLatitude;
    private final double fLongitude;
    private final Bitmap fImage;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public Tag(int id, double latitude, double longitude, Bitmap image) {
        fID = id;
        fLatitude = latitude;
        fLongitude = longitude;
        fImage = image;
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * This accessor returns the ID of the tag.
     *
     * @return The ID of the tag
     */
    public int getID() {
        return fID;
    }

    /**
     * This accessor returns the latitude of the tag.
     *
     * @return The latitude of the tag
     */
    public double getLatitude() {
        return fLatitude;
    }

    /**
     * This accessor returns the longitude of the tag.
     *
     * @return The longitude of the tag
     */
    public double getLongitude() {
        return fLongitude;
    }

    /**
     * This accessor returns the image of the tag.
     *
     * @return The image of the tag
     */
    public Bitmap getImage() {
        return fImage;
    }

}
