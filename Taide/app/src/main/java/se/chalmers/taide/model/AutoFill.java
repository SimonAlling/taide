package se.chalmers.taide.model;

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
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    String getPrefix(String source, int index);


    /**
     * Retrieves the text that should be placed after the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed after the selection marker
     */
    String getSuffix(String source, int index);
}