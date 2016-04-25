package se.chalmers.taide.model;

import java.util.Collections;
import java.util.List;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-29.
 *
 * Basically applies auto fills (connects to text view and handles the
 * communication with the auto fill objects).
 */
public class SimpleAutoFiller extends AbstractTextFilter{

    private List<AutoFill> autoFills;

    /**
     * Init the filter with a language
     * @param lang The language to use.
     */
    protected SimpleAutoFiller(Language lang){
        this(lang == null ? Collections.<AutoFill>emptyList() : lang.getAutoFills());
    }

    /**
     * Init the filter with a list of auto fills immediately.
     * @param autoFills The auto fills to use
     */
    protected SimpleAutoFiller(List<AutoFill> autoFills) {
        this.autoFills = autoFills;
        String[] triggers = new String[autoFills.size()];
        for (int i = 0; i < autoFills.size(); i++){
            triggers[i] = autoFills.get(i).getTrigger();
        }
        setTriggerText(triggers);
    }

    /**
     * [FUNCTIONAL] Checks whether a matching autofill was found.
     * @param autoFill The autofill we want to check for
     * @param trigger The string that triggered an autofill
     * @return <code>true</code> if it was a match
     */
    private static boolean matchingAutoFill(AutoFill autoFill, String trigger) {
        return autoFill.getTrigger().equals(trigger);
    }

    /**
     * This method is called when some trigger has been detected
     * @param trigger The string that triggered the effect
     */
    @Override
    protected void applyFilterEffect(String trigger) {
        TextSource textView = getTextView();
        int caretPosition = textView.getSelectionStart();
        int triggerStartPosition = caretPosition - trigger.length();
        for (AutoFill autoFill : autoFills) {
            // If it was this autofill that was triggered, apply it:
            if (matchingAutoFill(autoFill, trigger)) {
                String prefix = autoFill.getPrefix(textView.getText().toString(), caretPosition);
                String suffix = autoFill.getSuffix(textView.getText().toString(), caretPosition);
                int triggerEndPosition = caretPosition + autoFill.selectionIncreaseCount(textView.getText().toString(), caretPosition);

                textView.getText().replace(triggerStartPosition, triggerEndPosition, prefix + suffix);
                textView.setSelection(caretPosition + prefix.length() - trigger.length());
            }
        }
    }

    /**
     * Checks whether this shortFormat string has any auto fill and retrieves its
     * replacer if such exists.
     * @param shortFormat The short format for the auto fill (the trigger)
     * @param source The current source code
     * @param index The current location of the selection in the code
     * @return The replacer of the shortFormat, or null if not existing
     */
    public String getAutoFillReplacement(String shortFormat, String source, int index){
        for(AutoFill autoFill : autoFills){
            if(autoFill.getTrigger().equalsIgnoreCase(shortFormat)){
                return autoFill.getPrefix(source, index)+autoFill.getSuffix(source, index);
            }
        }
        return null;
    }
}
