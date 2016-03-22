package se.chalmers.taide.model.history;

import java.util.HashMap;
import java.util.Map;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public class HistoryHandlerFactory {

    private static Map<String, TextHistoryHandler> textHistoryHandlers = new HashMap<>();

    public static TextHistoryHandler getTextHistoryHandler(String name, TextSource editor){
        return getTextHistoryHandler(name, editor, false);
    }

    public static TextHistoryHandler getTextHistoryHandler(String name, TextSource editor, boolean forceRecreate){
        TextHistoryHandler thh;
        if(forceRecreate || !textHistoryHandlers.containsKey(name)){
            thh = new TimeTextHistoryHandler();
            textHistoryHandlers.put(name, thh);
        }else{
            thh = textHistoryHandlers.get(name);
        }

        thh.registerInputField(editor);
        return thh;
    }

    public static FileHistoryHandler getDefaultFileHistoryHandler(){
        throw new UnsupportedOperationException("CodeFile handling is not yet implemented.");
    }
}
