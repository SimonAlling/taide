package se.chalmers.taide.model.languages;

import android.content.Context;
import android.util.Log;

/**
 * Created by Matz on 2016-02-07.
 *
 * Factory for creating language instances.
 */
public class LanguageFactory {

    public static final String JAVA = "java";
    public static final String DEFAULT_LANGUAGE = JAVA;

    /**
     * Instantiate a language object of default type (see DEFAULT_LANGUAGE)
     * @param context The current application context
     * @return A language instance of the default language
     */
    public static Language getDefaultLanguage(Context context){
        return getLanguage(null, context);
    }

    /**
     * Instantiate a language object based on the name parameter.
     * @param name The name of the language (default is LanguageFactory.JAVA)
     * @param context The current application context
     * @return A language instance representing the given programming language
     * @throws IllegalArgumentException If the given name is invalid (does not exist)
     */
    public static Language getLanguage(String name, Context context) throws IllegalArgumentException{
        if(name == null){
            Log.d("LanguageFactory", "No language provided, defaulting to Java");
            name = DEFAULT_LANGUAGE;
        }

        switch(name.toLowerCase()){
            case JAVA:    return new JavaImpl(context);
            default:      throw new IllegalArgumentException("No language with name '"+name+"' found.");
        }
    }

}
