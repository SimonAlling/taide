package se.chalmers.taide.model.languages;

import se.chalmers.taide.model.AutoFill;

/**
 * Created by Matz on 2016-02-29.
 *
 * The most basic implementation of an auto fill with static trigger, prefix
 * and suffix. The SimpleAutoFill objects are immutable.
 */
public class SimpleAutoFill implements AutoFill {

    private String trigger;
    private String prefix, suffix;

    /**
     * Initiate the object with the given values
     * @param trigger The trigger of the auto fill
     * @param prefix The prefix (this cannot be changed)
     * @param suffix The suffix (this cannot be changed)
     */
    public SimpleAutoFill(String trigger, String prefix, String suffix){
        this.trigger = trigger;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Retrieves the trigger to which this auto fill should react
     * @return The trigger text that should fire an event
     */
    public String getTrigger(){
        return trigger;
    }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    public String getPrefix(String source, int index){
        return prefix;
    }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    public String getSuffix(String source, int index){
        return suffix;
    }
}
