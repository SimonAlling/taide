package se.chalmers.taide.settings;

import android.preference.PreferenceActivity;

import java.util.ArrayList;
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

    // This will hold all the fragment types that we recognize and allow:
    private static List<String> validFragments = new ArrayList<>();

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preferences, target);
        // Put all fragments from the XML in our list of valid fragments:
        for (Header header : target) {
            validFragments.add(header.fragment);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return validFragments.contains(fragmentName);
    }

}
