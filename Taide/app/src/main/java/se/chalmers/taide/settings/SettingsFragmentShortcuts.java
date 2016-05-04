package se.chalmers.taide.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import se.chalmers.taide.R;

/**
 * Created by alling on 2016-05-02.
 */
public class SettingsFragmentShortcuts extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_shortcuts);
    }
}
