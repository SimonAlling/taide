package se.chalmers.taide.model;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 *
 * Filter for applying auto-indent effects (stuff that happens
 * when you press enter). This does also include fill-in methods
 * like auto completing curly brackets, advanced comments etc.
 */
public class SimpleAutoIndenter extends AbstractTextFilter {

    /**
     * Initiate obejct and setup triggers (new line character)
     * @param lang The language to use
     */
    protected SimpleAutoIndenter(Language lang){
        super("\n");
        setLanguage(lang);
    }

    /**
     * Applies the effect on the text view.
     * NOTE: This requires a non-null language to work. Note that no
     * check is made to make sure that the language is set.
     * @param trigger The string that triggered the effect
     */
    protected void applyFilterEffect(String trigger){
        //Retrieve variables
        TextSource codeView = getTextView();
        String source = codeView.getText().toString();
        int start = codeView.getSelectionStart();

        //Only apply if last character entered was new line character
        if(start > 0 && source.charAt(start-1) == '\n') {
            //Calculate changes
            int index = Math.max(0, source.lastIndexOf('\n', Math.max(0, start - 2)));
            String lastLine = source.substring(index + 1, start-1);
            String prefix = getLanguage().getIndentationPrefix(source, index+1, lastLine);
            String suffix = getLanguage().getIndentationSuffix(source, index+1, lastLine);

            //Apply changes
            codeView.getText().replace(start, start, prefix+suffix);
            codeView.setSelection(start+prefix.length());
        }
    }
}
