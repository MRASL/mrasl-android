package ca.polymtl.mrasl.ui.dialog;

import android.content.Context;
import android.util.AttributeSet;

/**
 * This class creates a preference dialog for choosing a frequency.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class FrequencyPickerDialog extends NumberPickerDialog {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final int MIN_VALUE = 5;
    private static final int MAX_VALUE = 60;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public FrequencyPickerDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrequencyPickerDialog(Context context, AttributeSet attrs, int defStyleAttr) {
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
