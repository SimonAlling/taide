package se.chalmers.taide.model.history;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public interface TextHistoryHandler extends HistoryHandler{

    /**
     * Register text view that changes should be recorded in
     * @param input The text view to observe
     */
    void registerInputField(TextSource input);

}
