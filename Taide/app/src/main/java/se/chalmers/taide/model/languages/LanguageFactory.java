package se.chalmers.taide.model.languages;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by Matz on 2016-02-07.
 *
 * Factory for creating language instances.
 */
public class LanguageFactory {

    public static final String JAVA = JavaImpl.NAME;
    public static final String XML = XMLImpl.NAME;
    public static final String TEXT = SimpleLanguage.NAME;

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
     * @param resources The current application context resources
     * @return A language instance representing the given programming language
     * @throws IllegalArgumentException If the given name is invalid (does not exist)
     */
    public static Language getLanguage(String name, Resources resources) throws IllegalArgumentException {
        switch (name) {
            case JAVA: return new JavaImpl(resources);
            case XML: return new XMLImpl(resources);
            case TEXT: return new SimpleLanguage();
            default:
                Log.d(LanguageFactory.class.getSimpleName(), name + " is not a recognized language. Defaulting to "+TEXT+".");
                return new SimpleLanguage();
        }
    }

    /**
     * Retrieves the language based on the given file format
     * @param fileformat The file ending of the language
     * @param resources The current application context resources
     * @return A language instance representing the given programming language
     */
    public static Language getLanguageFromFileFormat(String fileformat, Resources resources){
        String concreteName = null;
        switch(fileformat){
            case "java":    concreteName = JAVA;break;
            case "xml":     concreteName = XML;break;
        }

        return getLanguage(concreteName, resources);
    }

}
