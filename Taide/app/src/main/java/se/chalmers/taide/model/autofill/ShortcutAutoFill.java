package se.chalmers.taide.model.autofill;

import android.util.Log;

import se.chalmers.taide.util.StringUtil;

/**
 * Created by alling on 2016-04-29.
 */
public class ShortcutAutoFill extends SimpleAutoFill {
    // The string that must be appended to the trigger for it to be activated:
    public static final String TRIGGER_SUFFIX = " ";

    /**
     * Initiate the object with the given values
     *
     * @param trigger The trigger of the auto fill
     * @param prefix  The prefix (this cannot be changed)
     * @param suffix  The suffix (this cannot be changed)
     */
    public ShortcutAutoFill(String trigger, String prefix, String suffix) {
        super(trigger, prefix, suffix);
    }

    /**
     * Determines whether the autofill should trigger on this input.
     * @param text The current text
     * @param pos The position of the cursor in the text
     * @param preview If this is a preview (non-activatable) or the real trigger
     * @return <code>true</code> if the autofill should be triggered.
     */
    @Override
    public boolean allowTrigger(String text, int pos, boolean preview){
        String trigger = (preview ? getTrigger() : getSuffixedTrigger());
        return pos == trigger.length() || !Character.isLetter(text.charAt(pos - trigger.length() - 1));
    }

    @Override
    public String getTriggerSuffix() { return TRIGGER_SUFFIX; }

    @Override
    public String getSuffixedTrigger() {
        return getTrigger() + StringUtil.emptyIfNull(getTriggerSuffix());
    }
}
