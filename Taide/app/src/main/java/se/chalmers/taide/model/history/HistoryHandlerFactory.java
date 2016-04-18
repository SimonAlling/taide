package se.chalmers.taide.model.history;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public class HistoryHandlerFactory {

    private static Map<String, TextHistoryHandler> textHistoryHandlers = new HashMap<>();
    private static HistoryHandlerType historyHandlerType = HistoryHandlerType.WORD;

    public static TextHistoryHandler getTextHistoryHandler(String name, TextSource editor){
        return getTextHistoryHandler(name, editor, false);
    }

    public static TextHistoryHandler getTextHistoryHandler(String name, TextSource editor, boolean forceRecreate){
        TextHistoryHandler thh;
        if(forceRecreate || !textHistoryHandlers.containsKey(name)){
            thh = createTextHistoryHandler();
            textHistoryHandlers.put(name, thh);
        }else{
            thh = textHistoryHandlers.get(name);
            if(!historyHandlerType.isOfThisType(thh)){
                return getTextHistoryHandler(name, editor, true);
            }
        }

        thh.registerInputField(editor);
        return thh;
    }

    private static TextHistoryHandler createTextHistoryHandler(){
        Log.d("HistoryHandlerFactory", "Creating history handler, mode = " + historyHandlerType.name());
        switch (historyHandlerType){
            case TIME:  return new TimeTextHistoryHandler();
            case WORD:  return new WordTextHistoryHandler();
            default:    return new TimeTextHistoryHandler();
        }
    }

    public static void setHistoryHandlerType(HistoryHandlerType type){
        historyHandlerType = type;
    }

    public static FileHistoryHandler getDefaultFileHistoryHandler(){
        throw new UnsupportedOperationException("CodeFile handling is not yet implemented.");
    }

    public enum HistoryHandlerType{
        TIME, WORD;

        public boolean isOfThisType(HistoryHandler handler){
            switch(this){
                case TIME:  return handler instanceof TimeTextHistoryHandler;
                case WORD:  return handler instanceof WordTextHistoryHandler;
                default:    return true;
            }
        }
    }
}
