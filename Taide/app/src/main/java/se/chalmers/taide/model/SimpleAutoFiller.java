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
     * This method is called when some trigger has been detected
     * @param trigger The string that triggered the effect
     */
    @Override
    protected void applyFilterEffect(String trigger) {
        TextSource textView = getTextView();
        for (AutoFill f : autoFills) {
            // If it was this auto fill that triggered it, apply it.
            if (f.getTrigger().equals(trigger) && textView.getText().subSequence(0, textView.getSelectionStart()).toString().endsWith(trigger)) {
                int pos = textView.getSelectionStart();
                String prefix = f.getPrefix(textView.getText().toString(), pos);
                String suffix = f.getSuffix(textView.getText().toString(), pos);
                textView.getText().replace(pos - trigger.length(), pos+f.selectionIncreaseCount(textView.getText().toString(), pos), prefix + suffix);
                textView.setSelection(pos + prefix.length() - trigger.length());
            }
        }
    }
}
