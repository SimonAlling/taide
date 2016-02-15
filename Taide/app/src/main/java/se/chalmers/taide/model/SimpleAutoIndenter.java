package se.chalmers.taide.model;

import android.util.Log;
import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 */
public class SimpleAutoIndenter extends AbstractTextFilter {

    protected SimpleAutoIndenter(Language lang){
        super("\n");
        setLanguage(lang);
    }

    protected void applyFilterEffect(){
        EditText codeView = getTextView();
        String source = codeView.getText().toString();
        int start = codeView.getSelectionStart();
        if(source.charAt(start-1) == '\n') {
            int index = Math.max(0, source.lastIndexOf('\n', Math.max(0, start - 2)));
            String lastLine = source.substring(index + 1, start-1);
            Log.d("NANO", "Line is '" + lastLine + "'");
            String prefix = getLanguage().getIndentationPrefix(source, index+1, lastLine);
            String suffix = getLanguage().getIndentationSuffix(source, index+1, lastLine);

            codeView.getText().replace(start, start, prefix+suffix);
            codeView.setSelection(start+prefix.length());
        }
    }
}
