package se.chalmers.taide.model.languages;

import se.chalmers.taide.model.AutoFill;
import se.chalmers.taide.util.StringUtil;

/**
 * Created by Matz on 2016-02-29.
 *
 * The most basic implementation of an auto fill with static trigger, prefix
 * and suffix. The SimpleAutoFill objects are immutable.
 */
public class SimpleAutoFill implements AutoFill {

    public static final String TRIGGER_SUFFIX = null;

    private String trigger;
    private String prefix, suffix;

    /**
     * Initiate the object with the given values
     * @param trigger The trigger of the auto fill
     * @param prefix The prefix (this cannot be changed)
     * @param suffix The suffix (this cannot be changed)
     */
    public SimpleAutoFill(String trigger, String prefix, String suffix) {
        this.trigger = trigger;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Retrieves the trigger to which this auto fill should react
     * @return The trigger text that should fire an event
     */
    @Override
    public String getTrigger(){
        return trigger;
    }

    @Override
    public String getTriggerSuffix() { return TRIGGER_SUFFIX; }

    @Override
    public String getSuffixedTrigger() {
        return getTrigger() + StringUtil.emptyIfNull(getTriggerSuffix());
    }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    @Override
    public String getPrefix(String source, int index) {
        if (isChainReaction(source, index)) {
            // Already replaced. Stop to prevent chain reaction.
            return "";
        } else {
            return prefix;
        }
    }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    @Override
    public String getSuffix(String source, int index) {
        if (isChainReaction(source, index)){
            // Already replaced. Stop to prevent chain reaction.
            return "";
        } else {
            return suffix;
        }
    }

    /**
     * Retrieves the number of extra chars (forward) that should be considered as well
     * in the replacement algorithm.
     * @param source The source code
     * @param offset The offset of the current selection
     * @return The number of extra chars to be considered in the replacement
     */
    @Override
    public int selectionIncreaseCount(String source, int offset){
        return 0;
    }

    private boolean isChainReaction(String source, int index) {
        String concat = prefix + suffix;
        return index >= concat.length() && source.substring(index-concat.length(), index).equals(concat);
    }
}
