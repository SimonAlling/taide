package se.chalmers.taide.model;

import android.content.Context;
import android.widget.EditText;

import se.chalmers.taide.model.languages.SimpleAutoFill;

/**
 * Created by Matz on 2016-02-07.
 *
 * Factory for instantiating the model
 */
public class ModelFactory {

    /**
     * Creates a EditorModel with the given language and binds it to the
     * given text view
     * @param textSource The text source to attach
     * @param lang The name of the language to use (see LanguageFactory constants)
     * @return A valid EditorModel object with correct properties.
     */
    public static EditorModel createEditorModel(Context context, TextSource textSource, String lang) {
        return new SimpleEditorModel(context, textSource, lang);
    }

    /**
     * Creates a auto fill object that can be fed into the model for cool customization
     * @param trigger The text to trigger an event
     * @param prefix The text to be positioned before the selection marker
     * @param suffix The text to be positioned after the selection marker
     * @return A valid auto fill object with the given properties.
     */
    public static AutoFill createAutoFill(String trigger, String prefix, String suffix) {
        return new SimpleAutoFill(trigger, prefix, suffix);
    }

    public static TextSource editTextToTextSource(EditText editText) {
        return new EditTextSource(editText);
    }

}
