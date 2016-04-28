package se.chalmers.taide.model;

import java.util.List;

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
     * Activates a text filter on the input manually
     * @param filterName The name of the filter
     */
    void manuallyTriggerFilter(String filterName);

    /**
     * Checks whether the model wants to exchange any word on space input.
     * @return The replacer, or null if not existing
     */
    String getAutoFillReplacement();

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

    /**
     * Creates a file in the current folder
     * @param name The name of the new file.
     * @param isFolder <code>true</code> if the file should be a folder
     * @return A reference to the created file.
     */
    CodeFile createFile(String name, boolean isFolder);

    /**
     * Renames the given file.
     * @param file The file to rename
     * @param newName The new name of the file
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    boolean renameFile(CodeFile file, String newName);

    /**
     * Deletes the given file
     * @param file The file to remove
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    boolean deleteFile(CodeFile file);

    /**
     * Steps into the given folder. If null is provided, steps up one level.
     * @param dir The reference to the folder, or null to step up one level
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean gotoFolder(CodeFile dir);

    /**
     * Checks whether there is possible to move up one more level.
     * @return <code>false</code> if this is the top level, <code>true</code> otherwise
     */
    boolean canStepUpOneFile();

    /**
     * Retrieve a list of all the files in the currect directory
     * @return A list of the files in the current directory
     */
    List<CodeFile> getFilesInCurrentDir();

    /**
     * Returns the CodeFile representation of the given path in the current project.
     * @param name The path name of the file
     * @return The CodeFile representation of the file, null if not found
     */
    CodeFile findFileByName(String name);

    /**
     * Retrieve a list of the names of all existing projects
     * @return A list of the names of all existing projects
     */
    String[] getAvailableProjects();

    /**
     * Creates a project and sets it to the active one.
     * @param name The name of the new project
     * @param type The type of the project to create
     * @param listener Triggered when the project is loaded into memory
     */
    boolean createProject(String name, ProjectType type, FileSystem.OnProjectLoadListener listener);

    /**
     * Sets the project to use.
     * @param name The name of the project.
     * @param listener Triggered when the project is loaded into memory
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean setProject(String name, FileSystem.OnProjectLoadListener listener);

    /**
     * Retrieves the name of the active project.
     * @return The name of the active project
     */
    String getActiveProject();
}
