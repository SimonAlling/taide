package se.chalmers.taide.model;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
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
     * @param context The current Android context
     * @param text The text view to attach
     */
    protected SimpleEditorModel(Context context, TextSource text) {
        this.context = context;
        this.textFilters = new HashMap<>();
        this.fileSystem = FileSystemFactory.getFileSystem(context);

        // Setup text view and apply highlight immediately
        setTextSource(text);

        setupFilters();
    }

    private void setupFilters(){
        addFilter(FILTER_KEY_INDENTATION, new SimpleAutoIndenter(this.language));
        addFilter(FILTER_KEY_AUTOFILL, new SimpleAutoFiller(this.language));
        addFilter(FILTER_KEY_HIGHLIGHT, new SimpleHighlighter(this.language));
        manuallyTriggerFilter(FILTER_KEY_HIGHLIGHT);
    }

    private void addFilter(String name, TextFilter filter){
        if(this.textSource != null){
            filter.attach(this.textSource);
        }

        //Update list references
        TextFilter oldFilter = textFilters.put(name, filter);
        if(oldFilter != null){
            oldFilter.detach();
        }
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
            setupFilters();
        }
    }

    /**
     * Set the text view to be used for this model. If any other text
     * view is attached, this will be detached without any warning.
     * @param textSource The text source to set as the attached one.
     */
    @Override
    public void setTextSource(TextSource textSource) {
        if (textSource != null && textSource != this.textSource) {
            //Reset
            if(historyHandler != null){
                historyHandler.registerInputField(null);
            }

            // Replace and renew
            this.textSource = textSource;
            setupFilters();
            this.historyHandler = HistoryHandlerFactory.getTextHistoryHandler((currentFile==null?"":currentFile.getUniqueName()), textSource);
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
            if (historyHandler != null) {
                historyHandler.registerInputField(null); // Reset history handler
            }
            //Set language
            setLanguage(LanguageFactory.getLanguageFromFileFormat(currentFile.getFileFormat(), context.getResources()));

            //Change contents
            String contents = currentFile.getContents();
            if(textSource != null) {
                textSource.setText(contents);
            }
            //Assign new history handler
            this.historyHandler = HistoryHandlerFactory.getTextHistoryHandler((currentFile==null?"":currentFile.getUniqueName()), textSource);
            manuallyTriggerFilter(FILTER_KEY_HIGHLIGHT);
        }
    }

    /**
     * Saves the current file to storage
     * @param file The file to save
     */
    @Override
    public void saveFile(CodeFile file){
        if(file != null && textSource != null){
            Log.d("EditorModel", "Saving file [" + file.getName() + ", "+file.getClass().getSimpleName()+"] :: " + textSource.getText().length() + " chars");
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
                cf.saveContents(this.language.getDefaultContent(name));
            }
            return cf;
        }
    }

    /**
     * Renames the given file.
     * @param file The file to rename
     * @param newName The new name of the file
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    @Override
    public boolean renameFile(CodeFile file, String newName){
        //To make sure references are not lost when obejcts are recreated.
        if(file.equals(currentFile)){
            currentFile = file;
        }

        return file.rename(newName);
    }

    /**
     * Deletes the given file
     * @param file The file to remove
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    @Override
    public boolean deleteFile(CodeFile file){
        if(file.equals(currentFile)) {
            if (file.remove()) {
                currentFile = null;
                if(textSource != null) {
                    textSource.setText("");
                }
                return true;
            }
            return false;
        }else{
            return file.remove();
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
     * @param type The type of the project to create
     * @param listener Triggered when the project is loaded into memory
     */
    @Override
    public boolean createProject(String name, ProjectType type, FileSystem.OnProjectLoadListener listener){
        return fileSystem.newProject(name, type, listener);
    }

    /**
     * Sets the project to use.
     * @param name The name of the project.
     * @param listener Triggered when the project is loaded into memory
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    @Override
    public boolean setProject(String name, FileSystem.OnProjectLoadListener listener){
        return fileSystem.setProject(name, listener);
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
