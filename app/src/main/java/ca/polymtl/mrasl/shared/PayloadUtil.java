package ca.polymtl.mrasl.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class contains util methods that are used in the formatting of the payload before sending it
 * to the drone.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PayloadUtil {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int FLOAT_SIZE = 4;
    private static final int DOUBLE_SIZE = 8;
    private static final int BITS_IN_BYTE = 8;

    // ---------------------------------------------------------------------------------------------
    // Util methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Util method that converts a float into a byte array using a buffer.
     *
     * @param buffer The buffer to put the converted float
     * @param pos    The position to start putting the converted float
     * @param input  The float to convert
     */
    public static void putFloatToBytes(byte[] buffer, int pos, float input) {
        /* Get the integer representation in order bitwise operations */
        int value = Float.floatToIntBits(input);

        /* Convert the float into array of bytes */
        for (int i = pos; i < pos + FLOAT_SIZE; i++) {
            buffer[i] = (byte) (value >> (((FLOAT_SIZE - 1) - (i - pos)) * BITS_IN_BYTE));
        }
    }

    /**
     * Util method that converts a double into a byte array using a buffer.
     *
     * @param buffer The buffer to put the converted double
     * @param pos    The position to start putting the converted double
     * @param input  The double to convert
     */
    public static void putDoubleToBytes(byte[] buffer, int pos, double input) {
        /* Get the double representation in order bitwise operations */
        long value = Double.doubleToLongBits(input);

        /* Convert the double into array of bytes */
        for (int i = pos; i < pos + DOUBLE_SIZE; i++) {
            buffer[i] = (byte) (value >> (((DOUBLE_SIZE - 1) - (i - pos)) * BITS_IN_BYTE));
        }
    }

}
