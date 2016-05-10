package se.chalmers.taide.model.autofill;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.chalmers.taide.model.AbstractTextFilter;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-29.
 *
 * Basically applies auto fills (connects to text view and handles the
 * communication with the auto fill objects).
 */
public class SimpleAutoFiller extends AbstractTextFilter {

    private List<AutoFill> autoFills;
    private List<AutoFill> disabledAutoFills;
    private Context context;

    /**
     * Init the filter with a language
     * @param lang The language to use.
     */
    public SimpleAutoFiller(Context context, Language lang){
        this(context, lang == null ? Collections.<AutoFill>emptyList() : lang.getAutoFills());
        setLanguage(lang);
    }

    /**
     * Init the filter with a list of auto fills immediately.
     * @param autoFills The auto fills to use
     */
    protected SimpleAutoFiller(Context context, List<AutoFill> autoFills) {
        this.context = context;
        setAutoFills(autoFills);
        AutoFillFactory.registerAutofillChangeListener(new AutoFillFactory.AutoFillDataChangeListener() {
            @Override
            public void autoFillAdded(String category, AutoFill a) {
                Language lang = getLanguage();
                if (lang != null && lang.getName().equalsIgnoreCase(category)) {
                    SimpleAutoFiller.this.autoFills.add(a);
                    setAutoFills(SimpleAutoFiller.this.autoFills);
                }
            }

            @Override
            public void autoFillRemoved(String category, AutoFill a) {
                Language lang = getLanguage();
                if (lang != null && lang.getName().equalsIgnoreCase(category)) {
                    SimpleAutoFiller.this.autoFills.remove(a);
                    setAutoFills(SimpleAutoFiller.this.autoFills);
                }
            }
        });
    }

    @Override
    public void setLanguage(final Language lang){
        super.setLanguage(lang);
        if(lang != null) {
            AutoFillFactory.getAutofillsByCategory(context, lang.getName(), new AutoFillFactory.OnAutoFillsLoadedListener() {
                @Override
                public void onAutoFillsLoaded(List<AutoFill> autofills) {
                    if(autofills != null) {
                        autofills.addAll(lang.getAutoFills());
                        setAutoFills(autofills);
                    }
                }
            });
        }
    }

    protected void setAutoFills(List<AutoFill> autoFills){
        this.disabledAutoFills = new ArrayList<>();

        this.autoFills = autoFills;
        String[] triggers = new String[autoFills.size()];
        for (int i = 0; i < autoFills.size(); i++){
            triggers[i] = autoFills.get(i).getSuffixedTrigger();
        }
        setTriggerText(generateTriggerStrings(triggers));
    }

    /**
     * [FUNCTIONAL] Checks whether a matching autofill was found.
     * @param autoFill The autofill we want to check for
     * @param suffixedTrigger The string that triggered an autofill
     * @return <code>true</code> if it was a match
     */
    private static boolean matchingAutoFill(AutoFill autoFill, String suffixedTrigger) {
        return (autoFill.getSuffixedTrigger()).equals(suffixedTrigger.toLowerCase());
    }

    /**
     * This method is called when some trigger has been detected
     * @param trigger The string that triggered the effect
     * @param isOnAdd <code>true</code> if the event was triggered by an addition to the text source
     */
    @Override
    protected boolean applyFilterEffect(String trigger, boolean isOnAdd) {
        TextSource textView = getTextView();
        int caretPosition = textView.getSelectionStart();
        int triggerStartPosition = caretPosition - trigger.length();
        if(caretPosition != 0) {        //Don't trigger on file load
            for (AutoFill autoFill : autoFills) {
                // If it was this autofill that was triggered, apply it:
                if (matchingAutoFill(autoFill, trigger)) {
                    if (!disabledAutoFills.contains(autoFill)) {
                        String prefix = autoFill.getPrefix(textView.getText().toString(), caretPosition);
                        String suffix = autoFill.getSuffix(textView.getText().toString(), caretPosition);
                        int triggerEndPosition = caretPosition + autoFill.selectionIncreaseCount(textView.getText().toString(), caretPosition);

                        textView.getText().replace(triggerStartPosition, triggerEndPosition, prefix + suffix);
                        textView.setSelection(caretPosition + prefix.length() - trigger.length());
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether an autofill will be activated on space (or other trigger suffix) input.
     * @param source The current source code
     * @param index The current location of the selection in the code
     * @return The autofill, or null if not existing
     */
    public AutoFill getPotentialAutoFillReplacement(String source, int index) {
        final String sourceUntilIndex_lowercase = source.substring(0, index).toLowerCase();
        for (AutoFill autoFill : autoFills){
            final String autoFillTrigger_lowercase = autoFill.getTrigger().toLowerCase();
            if (autoFill.getTriggerSuffix() != null && sourceUntilIndex_lowercase.endsWith(autoFillTrigger_lowercase)) {
                return autoFill;
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
