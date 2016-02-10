package se.chalmers.taide.model.languages;

import android.content.Context;
import android.util.Log;

/**
 * Created by Matz on 2016-02-07.
 */
public class LanguageFactory {

    public static Language getLanguage(String name, Context context){
        if(name == null){
            Log.d("LanguageFactory", "No language provided, defaulting to Java");
            name = "Java";
        }

        switch(name.toLowerCase()){
            case "java":    return new JavaImpl(context);
            default:        return new JavaImpl(context);
        }
    }

}
