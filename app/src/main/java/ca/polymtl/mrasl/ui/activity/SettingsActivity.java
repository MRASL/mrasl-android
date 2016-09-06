package ca.polymtl.mrasl.ui.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import ca.polymtl.mrasl.R;

/**
 * This class handles the activity that let the user change the preferences of the application.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SettingsActivity extends PreferenceActivity {

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Create the fragment containing the settings */
        PreferenceFragment settings = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settings).commit();

    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }

}