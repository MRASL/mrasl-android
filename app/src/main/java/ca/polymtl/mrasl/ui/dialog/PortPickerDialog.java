package ca.polymtl.mrasl.ui.dialog;

import android.content.Context;
import android.util.AttributeSet;

/**
 * This class creates a preference dialog for choosing a port number.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class PortPickerDialog extends NumberPickerDialog {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 65535;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public PortPickerDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortPickerDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    protected int getMin() {
        return MIN_VALUE;
    }

    @Override
    protected int getMax() {
        return MAX_VALUE;
    }

}
