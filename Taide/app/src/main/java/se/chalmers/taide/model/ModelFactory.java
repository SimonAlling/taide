package se.chalmers.taide.model;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-07.
 *
 * Factory for instantiating the model
 */
public class ModelFactory {

    /**
     * Creates a EditorModel with the given language and binds it to the
     * given text view
     * @param text The text view to attach
     * @param lang The name of the language to use (see LanguageFactory constants)
     * @return A valid EditorModel object with correct properties.
     */
    public static EditorModel createEditorModel(EditText text, String lang){
        return new SimpleEditorModel(text, lang);
    }

}
