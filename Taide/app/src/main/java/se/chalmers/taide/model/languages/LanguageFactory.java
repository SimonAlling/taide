package se.chalmers.taide.model.languages;

import android.content.res.Resources;
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
     * @param resources The current application context resources
     * @return A language instance of the default language
     */
    public static Language getDefaultLanguage(Resources resources) {
        return getLanguage(null, resources);
    }

    /**
     * Instantiate a language object based on the name parameter.
     * @param name The name of the language (default is LanguageFactory.JAVA)
     * @param resouces The current application context resources
     * @return A language instance representing the given programming language
     * @throws IllegalArgumentException If the given name is invalid (does not exist)
     */
    public static Language getLanguage(String name, Resources resouces) throws IllegalArgumentException {
        if (name == null) {
            Log.d("LanguageFactory", "No language provided, defaulting to Java");
            name = DEFAULT_LANGUAGE;
        }

        switch (name.toLowerCase()) {
            case JAVA: return new JavaImpl(resouces);
            default:   throw new IllegalArgumentException("No language with name '"+name+"' found.");
        }
    }

}
