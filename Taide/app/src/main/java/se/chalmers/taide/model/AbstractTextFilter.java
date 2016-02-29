package se.chalmers.taide.model;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 *
 * Implements the basic functionality for a text filter.
 */
public abstract class AbstractTextFilter implements TextFilter, TextWatcher{

    private Language language;
    private EditText editText;
    private String[] triggerTexts;

    protected AbstractTextFilter(String... triggerTexts){
        this.triggerTexts = triggerTexts;
    }

    /**
     * Perform the functionality of this specific filter.
     * @param trigger The string that triggered the effect
     */
    protected abstract void applyFilterEffect(String trigger);

    /**
     * Retrieve the text view that has been attached to this filter.
     * @return The currently attached text view
     */
    protected EditText getTextView(){
        return editText;
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
     * @param editText The text view to attach
     * @throws IllegalStateException If a text view is already attached to this filter.
     */
    public void attach(EditText editText) throws IllegalStateException{
        if(this.editText != null){
            throw new IllegalStateException("Cannot attach to new EditText, an attachment is already in use");
        }

        this.editText = editText;
        this.editText.addTextChangedListener(this);
    }

    /**
     * Detach the current text view from this filter. If no text view is
     * attached, this call will do nothing.
     */
    public void detach(){
        if(this.editText != null) {
            this.editText.removeTextChangedListener(this);
            this.editText = null;
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
     * Set the strings that should trigger the filter to be applied.
     * NOTE that if you use multi-letter word, it will only trigger if the entire
     * word is entered at the exactly same time (if you paste it etc.)
     * @param triggerTexts The strings that should trigger the effect.
     */
    protected void setTriggerText(String... triggerTexts){
        this.triggerTexts = triggerTexts;
    }


    /**
     * Triggered when anything changes in the text field. Calls applyFilterEffect()
     * if any of the trigger texts are inserted.
     * @param s The contents of the text view
     * @param start The start of the change
     * @param before The previous length of the data (starting at start)
     * @param count The new length of the data (starting at start)
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Update filter if correct input received
        if(count>=before) {
            String input = s.toString().substring(0, start + count);
            for (String triggerText : triggerTexts) {
                if (input.endsWith(triggerText)) {
                    applyFilterEffect(triggerText);
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

}
