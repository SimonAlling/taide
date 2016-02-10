package se.chalmers.taide.model;

import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 */
public interface TextFilter {

    void attach(EditText text);
    void detach();
    void setLanguage(Language lang);
}
