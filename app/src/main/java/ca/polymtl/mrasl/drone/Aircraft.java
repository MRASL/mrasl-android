package ca.polymtl.mrasl.drone;

import ca.polymtl.mrasl.mission.Mission;
import ca.polymtl.mrasl.shared.IDisposable;
import dji.sdk.Battery.DJIBattery;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;

/**
 * This class represents the aircraft and its components.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Aircraft implements IDisposable {

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final DJIBaseProduct fProduct;
    private final DJIAircraft fAircraft;
    private final RadioLink fRadioLink;
    private final Mission fMission = new Mission();
    private final Camera fCamera;
    private final Gimbal fGimbal;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Factory constructor for an aircraft. It makes sure that the {@link DJIBaseProduct} is an
     * instance of {@link DJIAircraft}. It returns {@code null} if it is not.
     *
     * @param product The product to create an aircraft from
     *
     * @return The new aircraft
     */
    public static Aircraft createAircraft(DJIBaseProduct product) {
        /* Make sure we have a drone */
        if (!(product instanceof DJIAircraft)) {
            return null;
        }

        return new Aircraft(product);
    }

    /**
     * Constructor for an aircraft.
     *
     * @param product An instance of {@link DJIAircraft}
     */
    Aircraft(DJIBaseProduct product) {
        fProduct = product;
        fAircraft = (DJIAircraft) product;
        fRadioLink = new RadioLink(fAircraft, fMission);
        fCamera = Camera.createCamera(product, fMission);
        fGimbal = Gimbal.from(fAircraft.getGimbal());

        /* Set the payload manager of the mission */
        fMission.setPayloadManager(fRadioLink.getPayloadManager());
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        fRadioLink.dispose();
        fMission.dispose();

        /* Make sure there is a gimbal object */
        if (fGimbal != null) {
            fGimbal.dispose();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * This accessor returns the product of the aircraft.
     *
     * @return The product of the aircraft
     */
    public DJIBaseProduct getProduct() {
        return fProduct;
    }

    /**
     * This accessor returns the mission of the aircraft.
     *
     * @return The mission of the aircraft
     */
    public Mission getMission() {
        return fMission;
    }

    /**
     * This accessor returns the camera of the aircraft.
     *
     * @return The camera of the aircraft
     */
    public Camera getCamera() {
        return fCamera;
    }

    /**
     * This accessor returns the battery of the aircraft.
     *
     * @return The battery of the aircraft
     */
    public DJIBattery getBattery() {
        return fAircraft.getBattery();
    }

}
