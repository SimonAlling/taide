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
