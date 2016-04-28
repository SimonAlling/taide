package se.chalmers.taide.model;

import android.util.Log;

import java.util.ArrayList;
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
    private List<AutoFill> disabledAutoFills;

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
        this.disabledAutoFills = new ArrayList<>();

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
                if(!disabledAutoFills.contains(autoFill)) {
                    String prefix = autoFill.getPrefix(textView.getText().toString(), caretPosition);
                    String suffix = autoFill.getSuffix(textView.getText().toString(), caretPosition);
                    int triggerEndPosition = caretPosition + autoFill.selectionIncreaseCount(textView.getText().toString(), caretPosition);

                    textView.getText().replace(triggerStartPosition, triggerEndPosition, prefix + suffix);
                    textView.setSelection(caretPosition + prefix.length() - trigger.length());
                }
            }
        }
    }

    /**
     * Checks whether an autofill will be activated on space input.
     * @param source The current source code
     * @param index The current location of the selection in the code
     * @return The autofill, or null if not existing
     */
    public AutoFill getAutoFillReplacement(String source, int index){
        String sourceUntilIndex = source.substring(0, index).toLowerCase();
        for(AutoFill autoFill : autoFills){
            if(autoFill.getTrigger().endsWith(" ")) {
                String autoFillTrigger = autoFill.getTrigger().trim().toLowerCase();
                if (sourceUntilIndex.endsWith(autoFillTrigger)) {
                    return autoFill;
                }
            }
        }
        return null;
    }

    /**
     * Enables or disables a certain autofill word so that this does/does not
     * trigger on data input
     * @param word The word to apply the change on
     * @param enabled <code>true</code> to enable autofill on the word, <code>false</code> otherwise
     */
    public void setWordEnabled(String word, boolean enabled){
        for(AutoFill autofill : autoFills){
            if(autofill.getTrigger().equalsIgnoreCase(word)){
                if(enabled){
                    disabledAutoFills.remove(autofill);
                }else if(!disabledAutoFills.contains(autofill)) {
                    disabledAutoFills.add(autofill);
                }
                break;
            }
        }
    }
}
