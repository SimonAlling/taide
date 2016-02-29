package se.chalmers.taide.model;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 *
 * Interface for describing a text filter. A text filter is something that
 * can be applied to a text view and modify it as it seem best fits.
 */
public interface TextFilter {

    /**
     * Attach a text view to this text filter. If the content of the text
     * view changes, the filter will detect this and update itself.
     * @param text The text view to attach
     */
    void attach(TextSource text);

    /**
     * Detaches the current text view. If no text view is attached, it will behave
     * just like the world does when magicarp performs a splash: Nothing will happen.
     */
    void detach();

    /**
     * Sets the language this filter will use.
     * @param lang The language to use
     */
    void setLanguage(Language lang);
}
