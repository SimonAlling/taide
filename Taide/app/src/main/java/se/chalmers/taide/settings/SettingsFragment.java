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

    // Listener for detecting changes to shared preferences:
    private final SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            onPreferenceChange(sharedPreferences, key);
        }
    };

    public void onPreferenceChange(SharedPreferences sharedPreferences, String key) {
        // This will be done every time a preference change is detected.
        updateGUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize preferences handles:
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(changeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(changeListener);
    }

    protected void updateGUI() {
        // Override in subclasses if needed.
    }
}
