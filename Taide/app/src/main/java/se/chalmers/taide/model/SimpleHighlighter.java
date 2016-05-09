package se.chalmers.taide.model;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.SyntaxBlock;

/**
 * Created by Matz on 2016-02-10.
 *
 * Filter to apply highlight colors to the text.
 */
public class SimpleHighlighter extends AbstractTextFilter {

    /**
     * Initiate object and setup triggers.
     * @param lang The language to use
     */
    protected SimpleHighlighter(Language lang) {
        super(generateTriggerStrings(lang==null?new String[0]:lang.getSyntaxHighlightingTriggers()));
        this.setLanguage(lang);
    }

    /**
     * Sets the language to use
     * @param lang The language to use
     */
    @Override
    public void setLanguage(Language lang){
        super.setLanguage(lang);
        if(lang != null) {
            this.setTriggerText(generateTriggerStrings(lang.getSyntaxHighlightingTriggers()));
        }
    }

    /**
     * Applies the actual effect on the text view.
     * NOTE: This requires a non-null language to work.
     * @param trigger The string that triggered the effect
     * @param isOnAdd <code>true</code> if the event was triggered by an addition to the text source
     */
    @Override
    protected boolean applyFilterEffect(String trigger, boolean isOnAdd) {
        //Retrieve data
        TextSource codeView = getTextView();
        Language language = getLanguage();

        if (codeView != null && language != null) {
            // Construct the markup based on Language.getSyntaxBlocks()
            int start = codeView.getSelectionStart();
            SpannableString code = new SpannableString(codeView.getText().toString());
            for (SyntaxBlock sb : language.getSyntaxBlocks(codeView.getText().toString())) {
                code.setSpan(new ForegroundColorSpan(sb.getMarkupColor()), sb.getStartIndex(), sb.getEndIndex(), 0);
                if (sb.isBold()) {
                    code.setSpan(new StyleSpan(Typeface.BOLD), sb.getStartIndex(), sb.getEndIndex(), 0);
                }
                if (sb.isItalic()) {
                    code.setSpan(new StyleSpan(Typeface.ITALIC), sb.getStartIndex(), sb.getEndIndex(), 0);
                }
            }
            codeView.setSpannable(code);

            // Reset text marker
            codeView.setSelection(start);
        }

        return false;
    }

    @Override
    public int getPriority(){
        return super.getPriority()+1;
    }
}
