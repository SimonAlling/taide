package se.chalmers.taide.model.languages;

import java.util.List;

import se.chalmers.taide.model.autofill.AutoFill;

/**
 * Created by Matz on 2016-02-07.
 *
 * General description of a programming language and its functionality.
 *
 * Note: This might force the classes to grow big and ugly, and might
 * need some refactoring in the end.
 */
public interface Language {

    /**
     * Returns the name of the language
     * @return The name of the language
     */
    String getName();

    /**
     * Checks if this language uses the specified filename extension.
     * @param extension The extension to check against
     * @return <code>true</code> iff <code>extension</code> is used for files of this type
     */
    boolean usesFilenameExtension(String extension);

    /**
     * Checks if the given filename has an extension that is commonly used for this language.
     * @param filename The filename to check against
     * @return <code>true</code> iff <code>filename</code> ends with any one of the extensions
     * associated with this language
     */
    boolean matchesFilename(String filename);

    /**
     * Retrieves all syntax highlighting blocks for the particular language.
     * @param sourceCode The source code to parse
     * @return A list of SyntaxBlock where each block represents a highlight region
     */
    List<SyntaxBlock> getSyntaxBlocks(String sourceCode);

    /**
     * Retrieves an array of all the string which should trigger a highlight filter. If "" is
     * returned as any of the indexes, the highlight will trigger on all input.
     * @return The triggers that should trigger a highlight filter.
     */
    String[] getSyntaxHighlightingTriggers();

    /**
     * Retrieves the indentation text to insert on a newline character. This text will
     * be positioned before the input marker.
     * @param source The entire source code
     * @param start The start index in the the source code of the line that was ended
     * @param line The line that was ended
     * @return The text to insert at the beginning of the new line
     */
    String getIndentationPrefix(String source, int start, String line);

    /**
     * Retrieves the indentation text to insert on a newline character, but position
     * after the text input marker.
     * @param source The entire source code
     * @param start The start index in the the source code of the line that was ended
     * @param line The line that was ended
     * @return The text to insert on the new line, after the line marker
     */
    String getIndentationSuffix(String source, int start, String line);

    /**
     * Check whether the char at the given index is inside a
     * comment or not.
     * @param source The source code
     * @param index The source character offset to investigate
     * @return <code>true</code> if the character is inside a comment, <code>false</code> otherwise
     */
    boolean isInComment(String source, int index);

    /**
     * Retrieves a list of all the autofills that should be applied
     * @return A list of the language specific auto fills
     */
    List<AutoFill> getAutoFills();

    /**
     * Generates the default content a file should be filled with on creation.
     * @param filename The name of the file
     * @return Text content the new file should be filled with
     */
    String getDefaultContent(String filename);

}
