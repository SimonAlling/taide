package se.chalmers.taide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Collections;

import se.chalmers.taide.model.history.HistoryHandler;
import se.chalmers.taide.model.history.HistoryHandlerFactory;

/**
 * Created by Matz on 2016-04-07.
 */
public class SettingsFragment extends PreferenceFragment{

    public static final String PREFS_USE_SYNC = "pref_key_use_sync";
    public static final String PREFS_SYNC_DENY_FORMATS = "pref_key_sync_deny_formats";
    public static final String PREFS_HISTORY_HANDLER_TYPE = "pref_key_history_handler_type";

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
        updateHistorySettings(sharedPreferences);
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

    private void onPreferenceChange(SharedPreferences preferences, String key){
        switch(key){
            case PREFS_USE_SYNC: case PREFS_SYNC_DENY_FORMATS:  updateSyncPreferences(preferences); break;
            case PREFS_HISTORY_HANDLER_TYPE:                    updateHistorySettings(preferences);break;
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

    private void updateHistorySettings(SharedPreferences preferences){
        if(preferences != null){
            String type = preferences.getString(PREFS_HISTORY_HANDLER_TYPE, "time");
            switch(type){
                case "word":    HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.WORD);break;
                case "time":
                default:        HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.TIME);break;
            }
            Preference p = findPreference(PREFS_HISTORY_HANDLER_TYPE);
            if(p != null){
                p.setSummary("Current mode is " + type);
            }
        }
    }
}
