package se.chalmers.taide.model;

import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-07.
 */
public interface EditorModel {

    Language getLanguage();
    void setLanguage(Language lang);

    EditText getCurrentTextView();
    void setTextView(EditText view);

}
