package se.chalmers.taide.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


/**
 * Created by alling on 2016-05-03.
 */
public abstract class SettingsFragment extends PreferenceFragment {
    // All settings fragments should extend this class.

    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize preferences handles:
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferencesEditor = sharedPreferences.edit();
    }
}
