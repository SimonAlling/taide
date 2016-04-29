package se.chalmers.taide.model;

import android.util.Log;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.TabUtil;

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
    protected SimpleAutoIndenter(Language lang) {
        super(new TriggerString("\n", true), new TriggerString(" ", true), new TriggerString(" ", false));
        setLanguage(lang);
        setAllowChainingEvents(true);
    }

    /**
     * Applies the effect on the text view.
     * NOTE: This requires a non-null language to work. Note that no
     * check is made to make sure that the language is set.
     * @param trigger The string that triggered the effect
     * @param isOnAdd <code>true</code> if the event was triggered by an addition to the text source
     */
    protected boolean applyFilterEffect(String trigger, boolean isOnAdd) {
        // Retrieve variables
        TextSource codeView = getTextView();
        String source = codeView.getText().toString();
        int start = codeView.getSelectionStart();

        if(isOnAdd) {
            // Only apply if last character entered was new line character
            if (start > 0 && source.charAt(start-1)=='\n') {
                // Calculate changes
                int index = (start < 2 ? 0 : source.lastIndexOf('\n', start - 2) + 1);
                String lastLine = source.substring(index, start - 1);
                String prefix = getLanguage().getIndentationPrefix(source, index, lastLine);
                String suffix = getLanguage().getIndentationSuffix(source, index, lastLine);

                //Apply changes
                codeView.getText().replace(start, start, prefix + suffix);
                codeView.setSelection(start + prefix.length());
            } else if (start > 0 && source.charAt(start-1)==' ') {
                if(!TabUtil.usesTabs()) {
                    int lineStartIndex = source.lastIndexOf('\n', start - 1) + 1;
                    if (isOnlyTabs(source.substring(lineStartIndex, start - 1))) {
                        codeView.getText().replace(start - 1, start, TabUtil.getTabs(1));
                        return true;        //Consume event
                    }
                }
            }
        }else{
            if(trigger.equals(" ")){
                if(!TabUtil.usesTabs()) {
                    int lineStartIndex = (start>0?source.lastIndexOf('\n', start-1) + 1:0);
                    if (isOnlyTabs(source.substring(lineStartIndex, start) + " ")) {
                        int tabLength = TabUtil.getTabs(1).length();
                        codeView.getText().replace(start-tabLength+1, start, "");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isOnlyTabs(String str){
        String tab = TabUtil.getTabs(1);
        int tabLength = tab.length();
        if(str.length()%tabLength == 0){
            boolean isOnlyTabs = true;
            for(int i = 0; i<str.length(); i+=tabLength){
                if(!str.substring(i, i+tabLength).equals(tab)){
                    isOnlyTabs = false;
                    break;
                }
            }
            return isOnlyTabs;
        }else{
            return false;
        }
    }
}
