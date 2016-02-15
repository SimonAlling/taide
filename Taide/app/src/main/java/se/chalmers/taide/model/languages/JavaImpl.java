package se.chalmers.taide.model.languages;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import se.chalmers.taide.R;
import se.chalmers.taide.util.TabUtil;


/**
 * Created by Matz on 2016-02-07.
 *
 * Implementation for the Java programming language.
 */
public class JavaImpl extends SimpleLanguage{

    /* Java keywords */
    private static final String[][] keyWords = {new String[]{"boolean", "enum", "int", "double", "float", "long", "void", "char", "short", "byte", "String"},
                                                new String[]{"abstract", "static", "volatile", "native", "public", "private", "protected", "synchronized", "transient", "final", "strictfp"},
                                                new String[]{"continue", "for", "switch", "default", "do", "if", "else", "break", "throw", "case", "return", "catch", "try", "finally", "while", "import", "assert"},
                                                new String[]{"extends", "implements", "instanceof", "throws"},
                                                new String[]{"package", "class", "interface", "this", "super", "new"}};
    private int[] colors;

    protected JavaImpl(Context context){
        //Init syntax highlightning colors.
        colors = context.getResources().getIntArray(R.array.java_syntax_default_colors);
    }

    /**
     * Returns the name of the language
     * @return The name of the language
     */
    public String getName(){
        return "Java";
    }

    /**
     * Retrieves all syntax highlighting blocks for the particular language.
     * @param sourceCode The source code to parse
     * @return A list of SyntaxBlock where each block represents a highlight region
     */
    public List<SyntaxBlock> getSyntaxBlocks(String sourceCode){
        //Very basic and intuitive search-through. NOTE: Totally ineffective, just temporary.
        int inQuotes = -1;
        List<SyntaxBlock> res = new LinkedList<SyntaxBlock>();
        for(int i = 0; i<sourceCode.length(); i++){
            //Check for quotes
            if(sourceCode.charAt(i) == '\"' && (i == 0 || sourceCode.charAt(i-1) != '\\')){
                if(inQuotes >= 0){
                    res.add(new SimpleSyntaxBlock(inQuotes, i+1, colors[colors.length-1]));
                    inQuotes = -1;
                }else{
                    inQuotes = i;
                }
            }

            //Check for keywords. Note that they cannot happen if preceded by a letter or digit.
            if(i == 0 || (!Character.isLetter(sourceCode.charAt(i-1)) && !Character.isDigit(sourceCode.charAt(i-1)))){
                keyWordCheck:
                for (int j = 0; j < keyWords.length; j++) {
                    for (int k = 0; k < keyWords[j].length; k++) {
                        if (sourceCode.startsWith(keyWords[j][k], i)) {
                            //Found match! Save it.
                            res.add(new SimpleSyntaxBlock(i, i + keyWords[j][k].length(), colors[j]));
                            //Ignore the chars involved in the current word (no use parsing them)
                            i += keyWords[j][k].length();
                            break keyWordCheck;
                        }
                    }
                }
            }
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
    public String getIndentationPrefix(String source, int start, String line){
        String prefix = super.getIndentationPrefix(source, start, line);
        if(!isInComment(source, start+line.length())) {
            //Not in comment
            if (line.endsWith("{")) {
                prefix += TabUtil.getTabs(1);
            }
        }else{
            //In comment
            if (line.trim().startsWith("/*")){
                prefix += " * ";
            }else if(line.trim().startsWith("*")){
                prefix += "* ";
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
    public String getIndentationSuffix(String source, int start, String line){
        String suffix = super.getIndentationSuffix(source, start, line);
        if(!isInComment(source, start+line.length())){
            //Not in comment
            if (line.endsWith("{")) {
                if(countOccurrences(source, "{") > countOccurrences(source, "}")) {
                    suffix += "\n" + super.getIndentationPrefix(source, start, line) + "}";
                }
            }
        }else{
            //In comment
            if(line.trim().startsWith("/*")){
                if(!line.contains("*/")) {
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
    private boolean isInComment(String source, int index){
        int lineStart = Math.max(0, source.lastIndexOf("\n", index)+1);
        int lineEnd = source.indexOf("\n", lineStart);
        String line = source.substring(lineStart, (lineEnd<0?source.length():lineEnd));
        if(line.contains("//")){
            return true;
        }

        int longCommentStart = source.lastIndexOf("/*", index);
        return longCommentStart>=0 && source.lastIndexOf("*/", index) < longCommentStart;
    }

    /**
     * Counts the occurrences of a special needle that is not inside a comment.
     * @param source The entire source code
     * @param needle The needle to search for
     * @return The number of occurrences of the needle outside comments.
     */
    private int countOccurrences(String source, String needle){
        int index = 0, count = 0;
        while((index = source.indexOf(needle, index)) > 0){
            if(!isInComment(source, index)) {
                count++;
            }
            index++;
        }
        return count;
    }
}
