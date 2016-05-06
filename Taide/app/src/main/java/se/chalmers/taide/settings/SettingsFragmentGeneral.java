package se.chalmers.taide.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Collections;

import se.chalmers.taide.R;
import se.chalmers.taide.model.history.HistoryHandlerFactory;
import se.chalmers.taide.util.MathUtil;

/**
 * Created by alling on 2016-05-02.
 */
public class SettingsFragmentGeneral extends SettingsFragment {

    public static final String KEY_SYNC = "pref_key_use_sync";
    public static final String KEY_SYNC_FORMATS = "pref_key_sync_formats";
    public static final String KEY_HISTORY_HANDLER_TYPE = "pref_key_history_handler_type";

    private static final int UNIT_CHAR_PLURAL = R.string.unit_char_plural;
    private static final int UNIT_CHAR_ABBR = R.string.unit_char_abbr;
    private static final int UNIT_LENGTH = R.string.unit_cm;
    private static final int UNIT_LENGTH_ABBR = R.string.unit_cm_abbr;
    private static final int UNIT_PER = R.string.unit_per;
    private static final int UNIT_PER_ABBR = R.string.unit_per_abbr;
    private static final int SENSITIVITY_DECIMALS = 1;

    public String sensitivityDescription(double sensitivity) {
        return MathUtil.round(sensitivity, SENSITIVITY_DECIMALS) + " " + getString(UNIT_CHAR_PLURAL) + " " + getString(UNIT_PER) + " " + getString(UNIT_LENGTH);
    }

    public String sensitivityAbbreviation(double sensitivity) {
        return MathUtil.round(sensitivity, SENSITIVITY_DECIMALS) + " " + getString(UNIT_CHAR_ABBR) + getString(UNIT_PER_ABBR) + getString(UNIT_LENGTH_ABBR);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_general);
    }

    @Override
    protected void updateGUI() {
        updateGUI_sync(sharedPreferences);
    }

    // Update Sync related GUI elements:
    private void updateGUI_sync(SharedPreferences preferences) {
        // We'll start out with the assumption that no resource types are selected:
        int stringID = R.string.pref_summary_sync_formats_none;
        final int numberOfSelectedSyncFormats = preferences.getStringSet(KEY_SYNC_FORMATS, Collections.EMPTY_SET).size();
        if (numberOfSelectedSyncFormats == getResources().getStringArray(R.array.file_formats_values).length) {
            // All resource types are selected.
            stringID = R.string.pref_summary_sync_formats_all;
        } else if (numberOfSelectedSyncFormats > 0) {
            // Some, but not all, resource types are selected.
            stringID = R.string.pref_summary_sync_formats_some;
        }
        // Finally, we will consider the state of the Sync checkbox:
        final boolean syncEnabled = preferences.getBoolean(KEY_SYNC, true);
        if (!syncEnabled) { stringID = R.string.pref_summary_sync_formats_disabled; }
        findPreference(KEY_SYNC_FORMATS).setSummary(getString(stringID));
    }

    private void updateGUI_history(SharedPreferences preferences) {
        if (preferences != null) {
            String type = preferences.getString(KEY_HISTORY_HANDLER_TYPE, "time");
            switch (type) {
                case "word":
                    HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.WORD); break;
                case "time": default:
                    HistoryHandlerFactory.setHistoryHandlerType(HistoryHandlerFactory.HistoryHandlerType.TIME); break;
            }
            findPreference(KEY_HISTORY_HANDLER_TYPE).setSummary("Current mode is " + type);
        }
    }
}
