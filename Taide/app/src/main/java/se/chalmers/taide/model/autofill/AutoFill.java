package se.chalmers.taide.model.autofill;

/**
 * Created by Matz on 2016-02-29.
 *
 * Uses a trigger to be able to replace text with other text (as the name
 * hints: auto fill). Examples of usages for this are for replacing
 * syso with System.out.println or making an end parenthesis after a start
 * parenthesis.
 */
public interface AutoFill {

    /**
     * Retrieves the trigger to which this auto fill should react
     * @return The trigger text that should fire an event
     */
    String getTrigger();

    /**
     * Retrieves suffix that must follow the trigger for it to be activated.
     * @return The trigger suffix (which is null for most autofills)
     */
    String getTriggerSuffix();

    /**
     * Retrieves the trigger with the trigger suffix appended. If the suffix is null, this method
     * returns the same string as getTrigger.
     * @return The trigger and the suffix
     */
    String getSuffixedTrigger();

    /**
     * Determines whether the autofill should trigger on this input.
     * @param text The current text
     * @param pos The position of the cursor in the text
     * @param preview If this is a preview (non-activatable) or the real trigger
     * @return <code>true</code> if the autofill should be triggered.
     */
    boolean allowTrigger(String text, int pos, boolean preview);

    /**
     * Retrieves the pure savedata for the prefix. Do not use this for realtime autofilling.
     * @return The pure prefix data
     */
    String getPrefixData();

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    String getPrefix(String source, int index);

    /**
     * Retrieves the pure savedata for the suffix. Do not use this for realtime autofilling.
     * @return The pure suffix data
     */
    String getSuffixData();

    /**
     * Retrieves the text that should be placed after the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed after the selection marker
     */
    String getSuffix(String source, int index);

    /**
     * Retrieves the number of extra chars (forward) that should be considered as well
     * in the replacement algorithm.
     * @param source The source code
     * @param offset The offset of the current selection
     * @return The number of extra chars to be considered in the replacement
     */
    int selectionIncreaseCount(String source, int offset);
}
