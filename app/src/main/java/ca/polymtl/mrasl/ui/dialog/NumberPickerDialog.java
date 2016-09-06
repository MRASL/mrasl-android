package ca.polymtl.mrasl.ui.dialog;

import static android.widget.FrameLayout.LayoutParams;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

/**
 * This abstract class creates a preference dialog for picking a number.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class NumberPickerDialog extends DialogPreference {

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private FrameLayout fDialogView;
    private NumberPicker fPicker;
    private int value;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public NumberPickerDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPickerDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ---------------------------------------------------------------------------------------------
    // Abstract methods
    // ---------------------------------------------------------------------------------------------

    /**
     * This method returns the minimum number that can be selected.
     *
     * @return The minimum number possible
     */
    abstract protected int getMin();

    /**
     * This method returns the maximum number that can be selected.
     *
     * @return The maximum number possible
     */
    abstract protected int getMax();

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    protected View onCreateDialogView() {
        /* Create the layout for the dialog */
        LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        /* Create the number picker */
        fPicker = new NumberPicker(getContext());
        fPicker.setLayoutParams(params);

        /* Create a new frame layout */
        fDialogView = new FrameLayout(getContext());
        fDialogView.addView(fPicker);

        return fDialogView;
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        /* Configure the number picker */
        fPicker.setMinValue(getMin());
        fPicker.setMaxValue(getMax());
        fPicker.setWrapSelectorWheel(true);
        fPicker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean result) {
        super.onDialogClosed(result);

        /* Check if the dialog was canceled */
        if (!result) {
            return;
        }

        /* Remove the focus of the picker */
        fPicker.clearFocus();

        /* Set the new value */
        int newValue = fPicker.getValue();
        if (callChangeListener(newValue)) {
            setValue(newValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, getMin());
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(getMax()) : (Integer) defaultValue);
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessor that returns the currently selected value.
     *
     * @return The selected value
     */
    public int getValue() {
        return this.value;
    }

    // ---------------------------------------------------------------------------------------------
    // Mutators
    // ---------------------------------------------------------------------------------------------

    /**
     * Mutator that changes the selected value.
     *
     * @param value The new value of the dialog
     */
    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

}
