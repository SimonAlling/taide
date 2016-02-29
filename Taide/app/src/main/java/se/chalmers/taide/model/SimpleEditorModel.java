package se.chalmers.taide.model;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;

import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.chalmers.taide.model.history.HistoryHandlerFactory;
import se.chalmers.taide.model.history.TextHistoryHandler;
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

    private TextHistoryHandler historyHandler;
    private Language language;
    private EditText editText;

    private List<TextFilter> textFilters;

    /**
     * Initiate all data and add the basic filters to use
     * @param text The text view to attach
     * @param language The language to use
     */
    protected SimpleEditorModel(EditText text, String language){
        this.textFilters = new LinkedList<>();
        setLanguage(LanguageFactory.getLanguage(language, text.getContext()));

        //Init filters
        SimpleHighlighter sh = new SimpleHighlighter(this.language);
        textFilters.add(sh);
        textFilters.add(new SimpleAutoIndenter(this.language));
        textFilters.add(new SimpleAutoFiller(this.language));

        //Setup text view and apply highlight immediately
        setTextView(text);
        sh.applyFilterEffect("");
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
        if(lang != null && !lang.equals(this.language)){
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
                if(historyHandler != null){
                    historyHandler.registerInputField(null);        //Reset history handler
                }
                for(TextFilter tf : textFilters){
                    tf.detach();
                }
            }

            //Replace.
            this.editText = view;
            this.historyHandler = HistoryHandlerFactory.createTextHistoryHandler(editText);
            for(TextFilter tf : textFilters){
                tf.attach(view);
            }
        }
    }

    /**
     * Performs undo on the text field (according to the recorded history).
     * If no history is found, nothing is done.
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    @Override
    public boolean undo(){
        return historyHandler.undoAction();
    }

    /**
     * Retrieves a string describing what undo() will undo. E.g.
     * "Added 'public void'" or something similar. Returns null if no
     * history to undo is found.
     * @return A string containing what calling undo() will perform. null if no history is found.
     */
    @Override
    public String peekUndo(){
        return historyHandler.peekUndoAction();
    }

    /**
     * Performs redo on the text field (according to the recorded history).
     * If no history is found, nothing is done.
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    public boolean redo(){
        return historyHandler.redoAction();
    }

    /**
     * Retrieves a string describing what redo() will redo. E.g.
     * "Added 'public void'" or something similar. Returns null if no
     * history to redo is found.
     * @return A string containing what calling redo() will perform. null if no history is found.
     */
    public String peekRedo(){
        return historyHandler.peekRedoAction();
    }
}
