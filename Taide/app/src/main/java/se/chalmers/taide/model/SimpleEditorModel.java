package se.chalmers.taide.model;

import android.content.Context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
import se.chalmers.taide.model.filesystem.FileSystemFactory;
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
public class SimpleEditorModel implements EditorModel {

    private static final String FILTER_KEY_INDENTATION = "AutoIndenter";
    private static final String FILTER_KEY_AUTOFILL = "AutoFill";
    private static final String FILTER_KEY_HIGHLIGHT = "Highlighter";

    private TextHistoryHandler historyHandler;
    private Language language;
    private TextSource textSource;
    private FileSystem fileSystem;
    private CodeFile currentFile;
    private Context context;

    private Map<String, TextFilter> textFilters;

    /**
     * Initiate all data and add the basic filters to use
     * @param text The text view to attach
     * @param language The language to use
     */
    protected SimpleEditorModel(Context context, TextSource text, String language) {
        this.textFilters = new HashMap<>();
        this.fileSystem = FileSystemFactory.getFileSystem(context);
        setLanguage(LanguageFactory.getLanguage(language, text.getResources()));

        // Init filters
        textFilters.put(FILTER_KEY_HIGHLIGHT, new SimpleHighlighter(this.language));
        textFilters.put(FILTER_KEY_INDENTATION, new SimpleAutoIndenter(this.language));
        textFilters.put(FILTER_KEY_AUTOFILL, new SimpleAutoFiller(this.language));

        // Setup text view and apply highlight immediately
        setTextSource(text);
        manuallyTriggerFilter(FILTER_KEY_HIGHLIGHT);
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
        if (lang != null && !lang.equals(this.language)) {
            this.language = lang;
            for (TextFilter tf : textFilters.values()) {
                tf.setLanguage(lang);
            }
        }
    }

    /**
     * Set the text view to be used for this model. If any other text
     * view is attached, this will be detached without any warning.
     * @param textSource The text source to set as the attached one.
     */
    @Override
    public void setTextSource(TextSource textSource) {
        if (textSource != null) {
            if (this.textSource != null) {
                if (historyHandler != null) {
                    historyHandler.registerInputField(null); // Reset history handler
                }
                for (TextFilter tf : textFilters.values()) {
                    tf.detach();
                }
            }

            // Replace
            this.textSource = textSource;
            this.historyHandler = HistoryHandlerFactory.createTextHistoryHandler(textSource);
            for (TextFilter tf : textFilters.values()) {
                tf.attach(this.textSource);
            }
        }
    }

    /**
     * Activates a text filter on the input manually
     * @param filterName The name of the filter
     */
    @Override
    public void manuallyTriggerFilter(String filterName){
        if(textFilters.containsKey(filterName)){
            ((AbstractTextFilter)textFilters.get(filterName)).applyFilterEffect("");
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


    /**
     * Saves the current file and opens the given file in the current input source
     * @param file The file to open
     */
    @Override
    public void openFile(CodeFile file){
        saveFile(currentFile);

        if(file != null) {
            this.currentFile = file;
            String contents = file.getContents();
            textSource.setText(contents);
            manuallyTriggerFilter(FILTER_KEY_HIGHLIGHT);
        }
    }

    /**
     * Saves the current file to storage
     * @param file The file to save
     */
    @Override
    public void saveFile(CodeFile file){
        if(file != null){
            fileSystem.saveFile(file, textSource.getText().toString());
        }
    }

    /**
     * Creates a file in the current folder
     * @param name The name of the new file.
     * @param isFolder <code>true</code> if the file should be a folder
     * @return A reference to the created file.
     */
    @Override
    public CodeFile createFile(String name, boolean isFolder){
        if(isFolder){
            return fileSystem.createDir(name);
        }else{
            CodeFile cf = fileSystem.createFile(name);
            if(this.language != null){
                cf.saveContents(this.context, this.language.getDefaultContent(name));
            }
            return cf;
        }
    }

    /**
     * Steps into the given folder. If null is provided, steps up one level.
     * @param dir The reference to the folder, or null to step up one level
     * @return The contents of the directory
     */
    @Override
    public boolean gotoFolder(CodeFile dir){
        if(dir != null){
            return fileSystem.stepIntoDir(dir);
        }else{
            return fileSystem.stepUpOneLevel();
        }
    }

    /**
     * Checks whether there is possible to move up one more level.
     * @return <code>false</code> if this is the top level, <code>true</code> otherwise
     */
    @Override
    public boolean canStepUpOneFile(){
        return fileSystem.canStepUpOneLevel();
    }

    /**
     * Retrieve a list of all the files in the currect directory
     * @return A list of the files in the current directory
     */
    @Override
    public List<CodeFile> getFilesInCurrentDir(){
        return fileSystem.getFilesInCurrentDir();
    }

    /**
     * Retrieve a list of the names of all existing projects
     * @return A list of the names of all existing projects
     */
    @Override
    public String[] getAvailableProjects(){
        return fileSystem.getExistingProjects().toArray(new String[0]);
    }

    /**
     * Creates a project and sets it to the active one.
     * @param name The name of the new project
     */
    @Override
    public boolean createProject(String name){
        if(fileSystem.newProject(name)){
            return setProject(name);
        }

        return false;
    }

    /**
     * Sets the project to use.
     * @param name The name of the project.
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    @Override
    public boolean setProject(String name){
        return fileSystem.setProject(name);
    }

    /**
     * Retrieves the name of the active project.
     * @return The name of the active project
     */
    @Override
    public String getActiveProject(){
        return fileSystem.getCurrentProject();
    }
}
