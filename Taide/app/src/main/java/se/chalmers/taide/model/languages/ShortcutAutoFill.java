package se.chalmers.taide.model.languages;

import se.chalmers.taide.model.AutoFill;

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

    @Override
    public String getTriggerSuffix() { return TRIGGER_SUFFIX; }
}
