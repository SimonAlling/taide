package se.chalmers.taide.model;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.SyntaxBlock;

/**
 * Created by Matz on 2016-02-10.
 *
 * Filter to apply highlight colors to the text.
 */
public class SimpleHighlighter extends AbstractTextFilter{

    /**
     * Initiate object and setup triggers.
     * @param lang The language to use
     */
    protected SimpleHighlighter(Language lang){
        super(" ", "\n", "(");
        this.setLanguage(lang);
    }

    /**
     * Applies the actual effect on the text view.
     * NOTE: This requires a non-null language to work.
     */
    protected void applyFilterEffect(){
        //Retrieve data
        EditText codeView = getTextView();
        Language language = getLanguage();

        if(codeView != null && language != null) {
            //Construct the markup based on Language.getSyntaxBlocks()
            int start = codeView.getSelectionStart();
            SpannableString code = new SpannableString(codeView.getText().toString());
            for (SyntaxBlock sb : language.getSyntaxBlocks(codeView.getText().toString())) {
                code.setSpan(new ForegroundColorSpan(sb.getMarkupColor()), sb.getStartIndex(), sb.getEndIndex(), 0);
            }
            codeView.setText(code, TextView.BufferType.SPANNABLE);

            //Reset text marker
            codeView.setSelection(start);
        }
    }
}
