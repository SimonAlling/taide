package se.chalmers.taide.model;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.SyntaxBlock;

/**
 * Created by Matz on 2016-02-10.
 */
public class SimpleHighlighter extends AbstractTextFilter{

    protected SimpleHighlighter(Language lang){
        super(" ", "\n");
        this.setLanguage(lang);
    }


    protected void applyFilterEffect(){
        EditText codeView = getTextView();
        Language language = getLanguage();
        if(codeView != null && language != null) {
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
