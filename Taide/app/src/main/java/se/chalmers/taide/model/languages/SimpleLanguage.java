package se.chalmers.taide.model.languages;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.model.autofill.AutoFill;

/**
 * Created by Matz on 2016-02-15.
 *
 * This class could be thought of as an implementation of simple text: no syntax highlighting, no
 * default content, nothing really. Actual programming language implementations should extend this
 * class and override its methods with more specific behavior.
 */
public class SimpleLanguage implements Language {

    private String name;
    private String[] filenameExtensions;

    protected SimpleLanguage(String name, String[] filenameExtensions) {
        this.name = name;
        this.filenameExtensions = filenameExtensions;
    }

    public String getName() {
        return name;
    }

    public String[] getHighlightTriggers() {
        return new String[0];
    }

    public boolean isInComment(String source, int index) {
        return false;
    }

    /**
     * Returns the "actual name part" of a filename, i.e. the part before the last period.
     * May not always be the proper way of detecting the actual filename (e.g. .user.js).
     * @param filename The filename to remove any extension from
     * @return <code>filename</code> with the extension removed
     */
    public static String filenameWithoutExtension(String filename) {
        return filename.replaceFirst("\\.[^\\.]*$", "");
    }

    public boolean usesFilenameExtension(String extension) {
        for (String ext : filenameExtensions) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesFilename(String filename) {
        for (String ext : filenameExtensions) {
            if (filename.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the default content of a new file of this type.
     * @param filename The name of the file
     * @return Content for the new file
     */
    public String getDefaultContent(String filename) {
        return "";
    }

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
