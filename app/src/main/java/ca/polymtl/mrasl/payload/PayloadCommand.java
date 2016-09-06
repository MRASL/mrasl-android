package ca.polymtl.mrasl.payload;

import static ca.polymtl.mrasl.shared.PayloadUtil.putFloatToBytes;

/**
 * This class implements a payload for sending commands.
 * <p/>
 * [ 0 ] The command magic number define
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PayloadCommand implements IPayload {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int PAYLOAD_SIZE = 1;
    private static final int POS_CMD = 0;

    // ---------------------------------------------------------------------------------------------
    // Enumerations
    // ---------------------------------------------------------------------------------------------

    /**
     * This enumeration contains the type of command that can be sent to the aircraft.
     */
    public enum CommandType {
        /**
         * Defines the magic number for starting the mission
         */
        START_MISSION((byte) 0x0),
        /**
         * Defines the magic number for aborting the mission by landing.
         */
        ABORT_LANDING((byte) 0x1),
        /**
         * Defines the magic number for aborting the mission by hovering.
         */
        ABORT_MISSION((byte) 0x2);

        /* Contains the actual magic number */
        private final byte fMagicNumber;

        /**
         * Constructor.
         *
         * @param number The magic number of the command.
         */
        CommandType(byte number) {
            fMagicNumber = number;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final CommandType fCommand;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor for a command payload.
     *
     * @param command The type of command that the payload contains.
     */
    public PayloadCommand(CommandType command) {
        fCommand = command;
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[PAYLOAD_SIZE];

        /* Format the payload */
        payload[POS_CMD] = fCommand.fMagicNumber;

        return payload;
    }

}
