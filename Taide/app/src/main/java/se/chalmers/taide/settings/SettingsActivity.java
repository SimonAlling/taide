package se.chalmers.taide.settings;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import java.util.List;

import se.chalmers.taide.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    // All Fragments that are used in the settings menu MUST be included here:
    private static final Class[] VALID_FRAGMENTS = {
        SettingsFragmentGeneral.class,
        SettingsFragmentShortcuts.class
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.settings, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        for (Class c : VALID_FRAGMENTS) {
            if (c.getName().equals(fragmentName)) {
                return true;
            }
        }
        throw new RuntimeException(fragmentName + " cannot be used in the settings menu because it is not explicitly included in the list of valid fragments in " + getClass().getName() + ".");
    }

}
