package se.chalmers.taide.model;

import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-07.
 *
 * The main model interface that works towards the UI.
 */
public interface EditorModel {

    /**
     * Retrieve the programming language that is currently being used for the editor.
     * @return The programming language being used
     */
    Language getLanguage();

    /**
     * Set the programming language for the editor.
     * @param lang The language to use
     */
    void setLanguage(Language lang);

    /**
     * Retrieve the currently attached text view to this model
     * @return The currently attached text view
     */
    EditText getCurrentTextView();

    /**
     * Set the text view to be used for this model. If any other text
     * view is attached, this will be detached without any warning.
     * @param view The view to set as the attached one.
     */
    void setTextView(EditText view);

}
