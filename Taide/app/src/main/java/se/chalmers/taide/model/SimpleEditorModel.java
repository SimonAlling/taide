package se.chalmers.taide.model;

import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.LanguageFactory;

/**
 * Created by Matz on 2016-02-07.
 *
 * Basic EditorModel with support for adding text filters dynamically.
 * Currently the following filters are used:
 *   - Syntax highlighting (SimpleHighlighter)
 *   - Auto indentation (SimpleAutoIndenter)
 */
public class SimpleEditorModel implements EditorModel{

    private Language language;
    private EditText editText;

    private List<TextFilter> textFilters;

    /**
     * Initiate all data and add the basic filters to use
     * @param text The text view to attach
     * @param language The language to use
     */
    protected SimpleEditorModel(EditText text, String language){
        this.language = LanguageFactory.getLanguage(language, text.getContext());

        //Init filters
        this.textFilters = new LinkedList<>();
        SimpleHighlighter sh = new SimpleHighlighter(this.language);
        textFilters.add(sh);
        textFilters.add(new SimpleAutoIndenter(this.language));

        //Setup text view and apply highlight immediately
        setTextView(text);
        sh.applyFilterEffect();
    }

    /**
     * Retrieve the currently used language
     * @return The currently used language
     */
    @Override
    public Language getLanguage() {
        return language;
    }

    /**
     * Set the language to use
     * @param lang The language to use
     */
    @Override
    public void setLanguage(Language lang) {
        if(lang != null){
            this.language = lang;
            for(TextFilter tf : textFilters){
                tf.setLanguage(lang);
            }
        }
    }

    /**
     * Retrieve the current text view
     * @return The current text view
     */
    @Override
    public EditText getCurrentTextView() {
        return editText;
    }

    /**
     * Set the text view to be used for this model. If any other text
     * view is attached, this will be detached without any warning.
     * @param view The view to set as the attached one.
     */
    @Override
    public void setTextView(EditText view) {
        if(view != null){
            if(this.editText != null){
                for(TextFilter tf : textFilters){
                    tf.detach();
                }
            }

            //Replace.
            this.editText = view;
            for(TextFilter tf : textFilters){
                tf.attach(view);
            }
        }
    }
}
