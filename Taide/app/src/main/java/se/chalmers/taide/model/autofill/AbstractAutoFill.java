package se.chalmers.taide.model.autofill;

/**
 * Created by Matz on 2016-05-04.
 */
public abstract class AbstractAutoFill implements AutoFill{

    private String trigger;

    public AbstractAutoFill(String trigger){
        this.trigger = trigger;
    }

    /**
     * Retrieves the trigger to which this auto fill should react
     * @return The trigger text that should fire an event
     */
    @Override
    public String getTrigger(){
        return trigger;
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
        return !preview;
    }

    /**
     * Retrieves the pure savedata for the prefix. Do not use this for realtime autofilling.
     * @return The pure prefix data
     */
    @Override
    public String getPrefixData(){
        return getPrefix(null, -1);
    }

    /**
     * Retrieves the pure savedata for the suffix. Do not use this for realtime autofilling.
     * @return The pure suffix data
     */
    @Override
    public String getSuffixData(){
        return getSuffix(null, -1);
    }
}
