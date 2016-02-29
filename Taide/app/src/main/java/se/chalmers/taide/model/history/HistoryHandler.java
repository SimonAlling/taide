package se.chalmers.taide.model.history;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-17.
 */
public interface HistoryHandler {

    /**
     * Retrieves a textual description of what action will be performed if
     * undoTextAction() is called.
     * @return A textual description of the current undo action
     */
    String peekUndoAction();

    /**
     * Undo the last change that was performed on the text view
     * @return If the action was successful
     */
    boolean undoAction();

    /**
     * Retrieves a textual description of what action will be performed if
     * redoTextAction() is called
     * @return A textual description of the current redo action
     */
    String peekRedoAction();

    /**
     * Redo the last change that was performed with undo
     * @return If the action was successful
     */
    boolean redoAction();

}
