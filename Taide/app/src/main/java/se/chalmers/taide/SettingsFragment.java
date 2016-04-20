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

    public static final String PREF_USE_SYNC = "pref_key_use_sync";
    public static final String PREF_SYNC_FORMATS = "pref_key_sync_formats";
    public static final String PREF_HISTORY_HANDLER_TYPE = "pref_key_history_handler_type";

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
            case PREF_USE_SYNC: case PREF_SYNC_FORMATS:  updateSyncPreferences(preferences); break;
            case PREF_HISTORY_HANDLER_TYPE:                    updateHistorySettings(preferences);break;
        }
    }

    private void updateSyncPreferences(SharedPreferences preferences){
        if(preferences != null) {
            //Sync formats stuff
            // We'll start out with the assumption that no resource types are selected:
            int stringRef = R.string.pref_summary_sync_formats_none;
            int allowedResourceTypeCount = preferences.getStringSet(PREF_SYNC_FORMATS, Collections.EMPTY_SET).size();
            if (allowedResourceTypeCount == getResources().getStringArray(R.array.file_formats_values).length) {
                // All resource types are selected.
                stringRef = R.string.pref_summary_sync_formats_all;
            } else if (allowedResourceTypeCount > 0) {
                // Some, but not all, resource types are selected.
                stringRef = R.string.pref_summary_sync_formats_some;
            }
            System.out.println(findPreference(PREF_SYNC_FORMATS));
            findPreference(PREF_SYNC_FORMATS).setSummary(getResources().getString(stringRef));
        }
    }

    private void updateHistorySettings(SharedPreferences preferences){
        if(preferences != null){
            String type = preferences.getString(PREF_HISTORY_HANDLER_TYPE, "time");
            switch(type){
                case "word":    HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.WORD);break;
                case "time":
                default:        HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.TIME);break;
            }
            Preference p = findPreference(PREF_HISTORY_HANDLER_TYPE);
            if(p != null){
                p.setSummary("Current mode is " + type);
            }
        }
    }
}
