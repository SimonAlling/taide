package se.chalmers.taide.model.history;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public interface TextHistoryHandler extends HistoryHandler{

    /**
     * Register text view that changes should be recorded in. If input is null,
     * the history handler will become inactive.
     * @param input The text view to observe
     */
    void registerInputField(TextSource input);

    /**
     * Retrieves the currently bound text source to this handler.
     * @return The currently bound text source.
     */
    TextSource getTextSource();

}
