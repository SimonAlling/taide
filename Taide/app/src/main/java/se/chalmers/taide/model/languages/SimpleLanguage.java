package se.chalmers.taide.model.languages;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.model.autofill.AutoFill;

/**
 * Created by Matz on 2016-02-15.
 *
 * Implements the basic functionality that is shared among the most
 * programming languages, and leaves the rest of the methods abstract
 * for the actual language implementations to define.
 */
public abstract class SimpleLanguage implements Language {

    /**
     * Retrieves all syntax highlighting blocks for the particular language.
     * @param sourceCode The source code to parse
     * @return A list of SyntaxBlock where each block represents a highlight region (empty as default)
     */
    @Override
    public List<SyntaxBlock> getSyntaxBlocks(String sourceCode) {
        return new ArrayList<>();
    }

    /**
     * Retrieves the indentation text to insert on a newline character. This text will
     * be positioned before the input marker.
     * @param source The entire source code
     * @param start The start index in the the source code of the line that was ended
     * @param line The line that was ended
     * @return The text to insert at the beginning of the new line
     */
    @Override
    public String getIndentationPrefix(String source, int start, String line) {
        // Use all white space the previous line had by default
        String prefix = "";
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                prefix += " ";
            } else if (line.charAt(i) == '\t') {
                prefix += "\t";
            } else {
                break;
            }
        }

        return prefix;
    }

    /**
     * Retrieves the indentation text to insert on a newline character, but position
     * after the text input marker.
     * @param source The entire source code
     * @param start The start index in the the source code of the line that was ended
     * @param line The line that was ended
     * @return The text to insert on the new line, after the line marker
     */
    @Override
    public String getIndentationSuffix(String source, int start, String line) {
        //Return empty by default
        return "";
    }

    /**
     * Retrieves a list of all the auto fills that should be applied
     * @return A list of the language specific auto fills
     */
    @Override
    public List<AutoFill> getAutoFills(){
        return new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && this.getName().equals(((Language)obj).getName());
    }
}
