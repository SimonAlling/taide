package se.chalmers.taide.model;

import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
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
     * Set the text view to be used for this model. If any other text
     * view is attached, this will be detached without any warning.
     * @param textSource The text source to set as the attached one.
     */
    void setTextSource(TextSource textSource);

    /**
     * Retrieve the file system of this model
     * @return The current file system.
     */
    FileSystem getFileSystem();

    /**
     * Performs undo on the text field (according to the recorded history).
     * If no history is found, nothing is done.
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    boolean undo();

    /**
     * Retrieves a string describing what undo() will undo. E.g.
     * "Added 'public void'" or something similar. Returns null if no
     * history to undo is found.
     * @return A string containing what calling undo() will perform. null if no history is found.
     */
    String peekUndo();

    /**
     * Performs redo on the text field (according to the recorded history).
     * If no history is found, nothing is done.
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    boolean redo();

    /**
     * Retrieves a string describing what redo() will redo. E.g.
     * "Added 'public void'" or something similar. Returns null if no
     * history to redo is found.
     * @return A string containing what calling redo() will perform. null if no history is found.
     */
    String peekRedo();

    /**
     * Saves the current file and opens the given file in the current input source
     * @param file The file to open
     */
    void openFile(CodeFile file);

    /**
     * Saves the current file to storage
     * @param file The file to save
     */
    void saveFile(CodeFile file);

}
