package se.chalmers.taide.model;

import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.LanguageFactory;

/**
 * Created by Matz on 2016-02-07.
 */
public class SimpleEditorModel implements EditorModel{

    private Language language;
    private EditText editText;

    private List<TextFilter> textFilters;

    protected SimpleEditorModel(EditText text, String language){
        this.language = LanguageFactory.getLanguage(language, text.getContext());
        //Init filters
        this.textFilters = new LinkedList<>();
        SimpleHighlighter sh = new SimpleHighlighter(this.language);
        textFilters.add(sh);
        textFilters.add(new SimpleAutoIndenter());
        //Setup text view
        setTextView(text);
        //Highlight immediately
        sh.applyFilterEffect();
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language lang) {
        if(lang != null){
            this.language = lang;
            for(TextFilter tf : textFilters){
                tf.setLanguage(lang);
            }
        }
    }

    @Override
    public EditText getCurrentTextView() {
        return editText;
    }

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
