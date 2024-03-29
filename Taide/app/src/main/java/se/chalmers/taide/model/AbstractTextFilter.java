package se.chalmers.taide.model;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 *
 * Implements the basic functionality for a text filter.
 */
public abstract class AbstractTextFilter implements TextFilter, TextSource.TextSourceListener{

    private Language language;
    private TextSource textSource;
    private String previousInput = null;
    private TriggerString[] triggerTexts;
    private boolean allowChainingEvents = false;

    protected AbstractTextFilter(TriggerString... triggerTexts){
        setTriggerText(triggerTexts);
    }

    protected void setAllowChainingEvents(boolean allowChainingEvents){
        if(allowChainingEvents != this.allowChainingEvents){
            this.allowChainingEvents = allowChainingEvents;
            if(textSource != null) {
                textSource.removeListener(this);
                textSource.addListener(this, allowChainingEvents);
            }
        }
    }

    /**
     * Convert string array to a TriggerString array, with only onAdd properties.
     * @param texts The strings that should be converted
     */
    protected static TriggerString[] generateTriggerStrings(String... texts){
        TriggerString[] triggerTexts = new TriggerString[texts.length];
        for(int i = 0; i<triggerTexts.length; i++){
            triggerTexts[i] = new TriggerString(texts[i], true);
        }

        return triggerTexts;
    }

    /**
     * Perform the functionality of this specific filter.
     * @param trigger The string that triggered the effect
     * @param isOnAdd <code>true</code> if the event was triggered by an addition to the text source
     */
    protected abstract boolean applyFilterEffect(String trigger, boolean isOnAdd);

    /**
     * Retrieve the text view that has been attached to this filter.
     * @return The currently attached text view
     */
    protected TextSource getTextView(){
        return textSource;
    }

    /**
     * Retrieve the language that should be applied on the code in the given view
     * @return The programming language currently used in the text view
     */
    protected Language getLanguage(){
        return language;
    }


    /**
     * Attaches the given text view to this filter. Note that only one text
     * view can be attached to each instance at a time. To reuse the same filter,
     * detach before attaching the new text view.
     * @param textSource The text view to attach
     * @throws IllegalStateException If a text view is already attached to this filter.
     */
    @Override
    public void attach(TextSource textSource) throws IllegalStateException{
        if(this.textSource != null){
            throw new IllegalStateException("Cannot attach to new TextSource, an attachment is already in use");
        }

        this.textSource = textSource;
        this.textSource.addListener(this, allowChainingEvents);
    }

    /**
     * Detach the current text view from this filter. If no text view is
     * attached, this call will do nothing.
     */
    @Override
    public void detach() {
        if (this.textSource != null) {
            this.textSource.removeListener(this);
            this.textSource = null;
        }
    }

    /**
     * Set the language that should be used.
     * @param lang The language to use
     */
    public void setLanguage(Language lang){
        this.language = lang;
    }

    /**
     * Set the trigger strings that should trigger the filter to be applied.
     * @param texts The strings that should trigger the effect.
     */
    protected void setTriggerText(TriggerString... texts){
        this.triggerTexts = texts;
    }


    /**
     * Triggered when anything changes in the text field. Calls applyFilterEffect()
     * if any of the trigger texts are inserted.
     * @param textFieldContent The contents of the text field
     * @param start The start of the change
     * @param before The previous length of the data (starting at start)
     * @param count The new length of the data (starting at start)
     */
    @Override
    public boolean onTextChanged(String textFieldContent, int start, int before, int count) {
        // Update filter if correct input received
        final String input = textFieldContent.substring(0, start+count);
        final String input_lowercase = input.toLowerCase();
        final String prevInput = previousInput == null
                               ? ""
                               : previousInput.substring(0, Math.min(start+before, previousInput.length()));
        final String prevInput_lowercase = prevInput.toLowerCase();
        for (TriggerString triggerText : triggerTexts) {
            if((count>=before && triggerText.isOnAdd())) {
                if (input_lowercase.endsWith(triggerText.getTrigger())) {
                    if(applyFilterEffect(triggerText.getTrigger(), triggerText.isOnAdd())){
                        return true;
                    }
                }
            }else if((count<before && !triggerText.isOnAdd())){
                if(prevInput_lowercase.endsWith(triggerText.getTrigger())){
                    if(applyFilterEffect(triggerText.getTrigger(), triggerText.isOnAdd())){
                        return true;
                    }
                }
            }
        }
        previousInput = textFieldContent;
        return false;
    }

    @Override
    public int getPriority(){
        return 10;
    }


    public static class TriggerString{
        private String trigger;
        private boolean isOnAdd;
        public TriggerString(String trigger, boolean isOnAdd){
            this.trigger = trigger;
            this.isOnAdd = isOnAdd;
        }
        public String getTrigger(){
            return trigger;
        }
        public boolean isOnAdd(){
            return isOnAdd;
        }
    }
}
