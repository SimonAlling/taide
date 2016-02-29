package se.chalmers.taide.model.history;

import android.widget.EditText;

/**
 * Created by Matz on 2016-02-17.
 */
public class HistoryHandlerFactory {

    private static FileHistoryHandler fileHistoryHandler;

    public static TextHistoryHandler createTextHistoryHandler(EditText editor){
        TextHistoryHandler thh = new TimeTextHistoryHandler();
        thh.registerInputField(editor);

        return thh;
    }

    public static FileHistoryHandler getDefaultFileHistoryHandler(){
        throw new UnsupportedOperationException("File handling is not yet implemented.");
    }
}
