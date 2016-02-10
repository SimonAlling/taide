package se.chalmers.taide.model;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-10.
 */
public class SimpleAutoIndenter extends AbstractTextFilter {

    protected SimpleAutoIndenter(){
        super("\n");
    }

    protected void applyFilterEffect(){
        EditText codeView = getTextView();
        String source = codeView.getText().toString();
        int start = codeView.getSelectionStart();
        int index = Math.max(0, source.lastIndexOf('\n', Math.max(0, start-2)));
        String lastLine = source.substring(index+1, start);
        String indent = "";
        for(int i = 0; i<lastLine.length(); i++){
            if(lastLine.charAt(i) == ' '){
                indent += " ";
            }else if(lastLine.charAt(i) == '\t'){
                indent += "\t";
            }else{
                break;
            }
        }

        codeView.getText().replace(start, start, indent);
    }
}
