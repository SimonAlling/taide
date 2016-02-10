package se.chalmers.taide.model;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-07.
 */
public class ModelFactory {

    public static EditorModel createEditorModel(EditText text, String lang){
        return new SimpleEditorModel(text, lang);
    }

}
