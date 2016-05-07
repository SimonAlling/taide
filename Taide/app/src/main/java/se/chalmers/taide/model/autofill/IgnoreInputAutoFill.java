package se.chalmers.taide.model.autofill;

import se.chalmers.taide.util.StringUtil;

/**
 * Created by Matz on 2016-03-10.
 *
 * Extension of the auto fill functionality to be able to affect text outside
 * the actual trigger text. What should be replaced etc. is determined by the
 * content of the provided deciders (IgnoreDecider and ReplaceDecider). See
 * the interfaces for more information.
 */
public class IgnoreInputAutoFill extends AbstractAutoFill {

    public static final String TRIGGER_SUFFIX = null;

    private IgnoreDecider decider;
    private ReplaceDecider replaceDecider;

    public IgnoreInputAutoFill(String trigger, IgnoreDecider decider) {
        this(trigger, decider, null);
    }

    public IgnoreInputAutoFill(String trigger, IgnoreDecider decider, ReplaceDecider replaceDecider) {
        super(trigger);
        this.decider = decider;
        this.replaceDecider = replaceDecider;
    }

    @Override
    public String getTriggerSuffix() { return TRIGGER_SUFFIX; }

    public String getSuffixedTrigger() {
        return getTrigger() + StringUtil.emptyIfNull(getTriggerSuffix());
    }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param offset The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    @Override
    public String getPrefix(String source, int offset) {
        return (replaceDecider == null ? getTrigger() : replaceDecider.getReplacer(source, offset));
    }

    /**
     * Retrieves the text that should be placed after the selection marker
     * @param source The entire text that triggered this call
     * @param offset The index of the selection marker
     * @return The text that should be placed after the selection marker
     */
    @Override
    public String getSuffix(String source, int offset){
        return "";
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
        if (decider != null && decider.shouldIgnoreChar(source, offset)) {
            return (replaceDecider == null ? getTrigger().length() : replaceDecider.getReplacer(source, offset).length());
        } else {
            return 0;
        }
    }


    /**
     * Interface for determining whether to ignore a written character or not
     */
    public interface IgnoreDecider {
        /**
         * Check if the written char should be ignored. The char that is currently
         * being inserted can be found by calling source.charAt(offset).
         * @param source The source code
         * @param offset The offset to the index where the current selection is
         * @return <code>true</code> if the char/string should be ignored, <code>false</code> otherwise
         */
        boolean shouldIgnoreChar(String source, int offset);
    }

    /**
     * Interface for allowing dynamic replacements. When a replacement should be
     * performed, the getReplacer method is called with the current state params.
     */
    public interface ReplaceDecider {
        /**
         * Retrieves the text to replace the current trigger text with.
         * @param source The current source code
         * @param offset The current index offset of the selection
         * @return The text to use as a replacement
         */
        String getReplacer(String source, int offset);
    }
}
