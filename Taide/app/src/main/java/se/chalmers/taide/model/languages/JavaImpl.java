package se.chalmers.taide.model.languages;

import android.content.res.Resources;

import java.util.LinkedList;
import java.util.List;

import se.chalmers.taide.R;
import se.chalmers.taide.model.autofill.AutoFill;
import se.chalmers.taide.model.autofill.IgnoreInputAutoFill;
import se.chalmers.taide.util.StringUtil;
import se.chalmers.taide.util.TabUtil;


/**
 * Created by Matz on 2016-02-07.
 *
 * Implementation for the Java programming language.
 */
public class JavaImpl extends SimpleLanguage {

    public static final String NAME = "Java";
    public static final String[] FILENAME_EXTENSIONS = { "java" };
    public static final String[] SYNTAX_HIGHLIGHTING_TRIGGERS = {" ", "\n", "(", ")", "\"", "t"};

    /* Java keywords */
    // The ordering of elements in these arrays is NOT important:
    private static final String[] KEYWORDS_TYPES = new String[]{"boolean", "enum", "int", "double", "float", "long", "void", "char", "short", "byte"};
    private static final String[] KEYWORDS_MODIFIERS = new String[]{"abstract", "static", "volatile", "native", "public", "private", "protected", "synchronized", "transient", "final", "strictfp"};
    private static final String[] KEYWORDS_CONTROL_FLOW = new String[]{"continue", "for", "switch", "default", "do", "if", "else", "break", "throw", "case", "return", "catch", "try", "finally", "while", "import", "assert"};
    private static final String[] KEYWORDS_CLASS_DECLARATION = new String[]{"extends", "implements", "instanceof", "throws"};
    private static final String[] KEYWORDS_CLASS_RELATED = new String[]{"package", "class", "interface", "this", "super", "new"};

    private static final String[][] KEYWORDS = {
        // The order here IS important, since the syntax highlighting depends on it:
        KEYWORDS_TYPES,
        KEYWORDS_MODIFIERS,
        KEYWORDS_CONTROL_FLOW,
        KEYWORDS_CLASS_DECLARATION,
        KEYWORDS_CLASS_RELATED
    };

    private int[] colors;

    protected JavaImpl(Resources resources) {
        super(NAME, FILENAME_EXTENSIONS, SYNTAX_HIGHLIGHTING_TRIGGERS);
        //Init syntax highlightning colors.
        colors = resources.getIntArray(R.array.java_syntax_default_colors);
    }

    /**
     * Retrieves all syntax highlighting blocks for the particular language.
     * @param sourceCode The source code to parse
     * @return A list of SyntaxBlock where each block represents a highlight region
     */
    public List<SyntaxBlock> getSyntaxBlocks(String sourceCode) {
        // Very basic and intuitive search-through. NOTE: Totally ineffective, just temporary.
        int inQuotes = -1;
        int inComment = -1;
        boolean longComment = false;
        List<SyntaxBlock> res = new LinkedList<>();
        for (int i = 0; i < sourceCode.length(); i++) {
            if (inComment > 0) {
                if (longComment && sourceCode.charAt(i) == '*' && sourceCode.length() > i+1 && sourceCode.charAt(i+1) == '/') {
                    res.add(new SimpleSyntaxBlock(inComment, i + 2, colors[colors.length - 1], false, true));
                    inComment = -1;
                    i += 2;
                } else if (!longComment && sourceCode.charAt(i) == '\n') {
                    res.add(new SimpleSyntaxBlock(inComment, i + 1, colors[colors.length - 1], false, true));
                    inComment = -1;
                    i += 1;
                }
            }

            // Check for comment
            if (inQuotes < 0 && sourceCode.charAt(i) == '/' && sourceCode.length() > i+1) {
                if (sourceCode.charAt(i+1) == '*' || sourceCode.charAt(i+1) == '/') {
                    inComment = i;
                    longComment = sourceCode.charAt(i+1) == '*';
                }
            }
            // Check for quotes
            if (inComment < 0 && sourceCode.charAt(i) == '\"' && (i == 0 || sourceCode.charAt(i - 1) != '\\')) {
                if (inQuotes >= 0) {
                    res.add(new SimpleSyntaxBlock(inQuotes, i + 1, colors[colors.length - 2]));
                    inQuotes = -1;
                } else {
                    inQuotes = i;
                }
            }

            // Check for keywords. Note that they cannot happen if preceded by a letter or digit.
            if (inQuotes < 0 && inComment < 0 && (i == 0 || (!Character.isLetter(sourceCode.charAt(i - 1)) && !Character.isDigit(sourceCode.charAt(i - 1))))) {
                keyWordCheck:
                for (int j = 0; j < KEYWORDS.length; j++) {
                    for (int k = 0; k < KEYWORDS[j].length; k++) {
                        // Check for match and that the supposed syntactic block is not part of a longer word:
                        if (sourceCode.startsWith(KEYWORDS[j][k], i) && !StringUtil.isWordCharacter(sourceCode.charAt(i + KEYWORDS[j][k].length()))) {
                            // Found match! Save it.
                            res.add(new SimpleSyntaxBlock(i, i + KEYWORDS[j][k].length(), colors[j]));
                            // Ignore the chars involved in the current word (no use parsing them)
                            i += KEYWORDS[j][k].length();
                            break keyWordCheck;
                        }
                    }
                }
            }
        }

        // Add quotes and comments formatting manually if they do not end ever.
        if (inComment > 0) {
            res.add(new SimpleSyntaxBlock(inComment, sourceCode.length(), colors[colors.length - 1], false, true));
        } else if(inQuotes > 0) {
            res.add(new SimpleSyntaxBlock(inQuotes, sourceCode.length(), colors[colors.length - 2]));
        }

        return res;
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
        String prefix = super.getIndentationPrefix(source, start, line);
        if (!isInComment(source, start+line.length())) {
            // Not in comment
            if (line.trim().endsWith("{")) {
                prefix += TabUtil.getTabs(1, this);
            }
        } else {
            // In comment
            if (line.trim().startsWith("/*")) {
                prefix += " * ";
            } else if(line.trim().startsWith("*")) {
                prefix += "* ";
            }
        }

        return prefix;
    }

    /**
     * Retrieves the indentation text to insert on a newline character, but position
     * after the text input marker.
     * @param source The entire source code
     * @param start The start index of the line that was ended (in the the source code string)
     * @param line The line that was ended
     * @return The text to insert on the new line, after the line marker
     */
    @Override
    public String getIndentationSuffix(String source, int start, String line) {
        String suffix = super.getIndentationSuffix(source, start, line);
        if (!isInComment(source, start+line.length())) {
            // Not in comment
            if (line.trim().endsWith("{")) {
                if (countOccurrences(source, "{") > countOccurrences(source, "}")) {
                    suffix += "\n" + super.getIndentationPrefix(source, start, line) + "}";
                }else{
                    Character nextChar = StringUtil.nextNonWSChar(source, start+line.length());
                    if(nextChar != null && nextChar.charValue() == '}'){
                        suffix += "\n" + super.getIndentationPrefix(source, start, line);
                    }
                }
            }
        } else {
            // In comment
            if (line.trim().startsWith("/*")) {
                if (!line.contains("*/")) {
                    suffix += "\n" + super.getIndentationPrefix(source, start, line) + "*/";
                }
            }
        }

        return suffix;
    }

    /**
     * Check whether the char at the given index is inside a
     * comment or not.
     * @param source The source code
     * @param index The source character offset to investigate
     * @return <code>true</code> if the character is inside a comment, <code>false</code> otherwise
     */
    @Override
    public boolean isInComment(String source, int index){
        if(source == null || index<0){
            return false;
        }

        int lineStart = Math.max(0, source.lastIndexOf("\n", index)+1);
        int lineEnd = source.indexOf("\n", lineStart);
        String line = source.substring(lineStart, (lineEnd < 0 ? source.length() : lineEnd));
        if (line.contains("//")) {
            return true;
        }

        int longCommentStart = source.lastIndexOf("/*", index);
        return longCommentStart >= 0 && source.lastIndexOf("*/", index) < longCommentStart;
    }

    /**
     * Retrieves a list of all the autofills that should be applied
     * @return A list of the language specific auto fills
     */
    @Override
    public List<AutoFill> getAutoFills() {
        List<AutoFill> autoFills = new LinkedList<>();
        autoFills.add(new IgnoreInputAutoFill(")", new IgnoreInputAutoFill.IgnoreDecider() {
            @Override
            public boolean shouldIgnoreChar(String source, int offset) {
                //Ignore one ')' since the one is written now, might be overridden
                if(source != null) {
                    return source.length() > offset && source.charAt(offset) == ')' && countOccurrences(source, ")") - 1 == countOccurrences(source, "(");
                }else{
                    return false;
                }
            }
        }));
        autoFills.add(new IgnoreInputAutoFill("}", new IgnoreInputAutoFill.IgnoreDecider() {
            @Override
            public boolean shouldIgnoreChar(String source, int offset) {
                //Ignore one '}' since the one is written now, might be overridden
                if(source != null) {
                    return source.length() > offset && source.charAt(offset) == '}' && countOccurrences(source, "}") - 1 == countOccurrences(source, "{");
                }else{
                    return false;
                }
            }
        }));
        return autoFills;
    }

    /**
     * Generates the default content a file should be filled with on creation.
     * @param filename The name of the file
     * @return Text content the new file should be filled with
     */
    @Override
    public String getDefaultContent(String filename){
        // Remove everything from the first period and forward in the filename:
        final String className = filename.replaceFirst("\\..*$", "");
        return "public class "+className+" {\n"+TabUtil.getTabs(1, this)+"\n}";
    }

    /**
     * Counts the occurrences of a special needle that is not inside a comment.
     * @param source The entire source code
     * @param needle The needle to search for
     * @return The number of occurrences of the needle outside comments.
     */
    private int countOccurrences(String source, String needle) {
        int index = 0, count = 0;
        while ((index = source.indexOf(needle, index)) > 0) {
            if (!isInComment(source, index)) {
                count++;
            }
            index++;
        }
        return count;
    }
}
