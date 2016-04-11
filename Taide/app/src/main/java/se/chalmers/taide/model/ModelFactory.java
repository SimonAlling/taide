package se.chalmers.taide.model;

import android.content.Context;
import android.widget.EditText;

import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.model.languages.SimpleAutoFill;

/**
 * Created by Matz on 2016-02-07.
 *
 * Factory for instantiating the model
 */
public class ModelFactory {

    private static EditorModel currentModel;

    /**
     * Creates a EditorModel and binds it to the given text view
     * @param context The current context
     * @param textSource The text source to attach
     * @return A valid EditorModel object with correct properties.
     */
    public static EditorModel createEditorModel(Context context, TextSource textSource) {
        currentModel = new SimpleEditorModel(context, textSource);
        return currentModel;
    }

    /**
     * Tries to retrieve the current editor model. If none is created, returns null
     * @return The current editor model, or null if not found
     */
    public static EditorModel getCurrentEditorModel(){
        return currentModel;
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
        if(editText != null){
            return new EditTextSource(editText);
        }else{
            return null;
        }
    }

}
