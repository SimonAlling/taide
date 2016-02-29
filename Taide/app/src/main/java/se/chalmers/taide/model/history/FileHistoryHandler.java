package se.chalmers.taide.model.history;

/**
 * Created by Matz on 2016-02-17.
 */
public interface FileHistoryHandler extends HistoryHandler{

    /**
     * Register a change of file status (add, remove, rename).
     * @param action The action that happened
     * @param filename The filename of the file
     */
    void registerFileAction(Action action, String filename, String data);

    /**
     * Simple enum for handling different types of file actions
     */
    enum Action{
        ADD,
        DELETE,
        RENAME
    }

}
