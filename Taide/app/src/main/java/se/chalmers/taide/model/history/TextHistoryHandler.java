package se.chalmers.taide.model.history;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-17.
 */
public interface TextHistoryHandler extends HistoryHandler{

    /**
     * Register text view that changes should be recorded in
     * @param input The text view to observe
     */
    void registerInputField(EditText input);

}
