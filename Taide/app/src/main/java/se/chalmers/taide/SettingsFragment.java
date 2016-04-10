package se.chalmers.taide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;

import java.util.Collections;

/**
 * Created by Matz on 2016-04-07.
 */
public class SettingsFragment extends PreferenceFragment{

    public static final String PREFS_USE_SYNC = "pref_key_use_sync";
    public static final String PREFS_SYNC_DENY_FORMATS = "pref_key_sync_deny_formats";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            onPreferenceChange(sharedPreferences, key);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Add preferences menu
        addPreferencesFromResource(R.xml.preferences);
        updateSyncPreferences(sharedPreferences);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(changeListener);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(sharedPreferences != null){
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener);
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setEnabled(false);
    }

    private void onPreferenceChange(SharedPreferences preferences, String key){
        switch(key){
            case PREFS_USE_SYNC: case PREFS_SYNC_DENY_FORMATS: updateSyncPreferences(preferences); break;
        }
    }

    private void updateSyncPreferences(SharedPreferences preferences){
        if(preferences != null) {
            int stringRef;
            //Use sync stuff
            stringRef = preferences.getBoolean(PREFS_USE_SYNC, true) ? R.string.pref_summary_sync_on : R.string.pref_summary_sync_off;
            findPreference(PREFS_USE_SYNC).setSummary(getResources().getString(stringRef));

            //Sync formats stuff
            int allowedFormatCount = preferences.getStringSet(PREFS_SYNC_DENY_FORMATS, Collections.EMPTY_SET).size();
            stringRef = (allowedFormatCount > 0 ? R.string.pref_summary_sync_deny_formats_partly : R.string.pref_summary_sync_deny_formats_none);
            if (allowedFormatCount == getResources().getStringArray(R.array.file_formats_values).length) {
                stringRef = R.string.pref_summary_sync_deny_formats_all;
            }
            if (!preferences.getBoolean(PREFS_USE_SYNC, true)) {
                stringRef = R.string.pref_summary_sync_off;
            }
            findPreference(PREFS_SYNC_DENY_FORMATS).setSummary(getResources().getString(stringRef));
        }
    }
}
