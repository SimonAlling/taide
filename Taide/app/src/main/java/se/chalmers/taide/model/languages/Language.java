package se.chalmers.taide.model.languages;

import java.util.List;

import se.chalmers.taide.model.AutoFill;

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
     * Retrieves all syntax highlighting blocks for the particular language.
     * @param sourceCode The source code to parse
     * @return A list of SyntaxBlock where each block represents a highlight region
     */
    List<SyntaxBlock> getSyntaxBlocks(String sourceCode);

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

}
