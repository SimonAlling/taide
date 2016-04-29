package se.chalmers.taide.util;

/**
 * Created by Matz on 2016-03-07.
 */
public class StringUtil {

    public static final String WHITESPACE_CHARACTERS = " \n\t";

    /**
     * Repeats a string a specified number of times.
     * @param string The string to repeat
     * @param repetitions The number of repetitions
     * @return <code>string</code> repeated <code>repetitions</code> times.
     */
    
    public static String repeat(String string, int repetitions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repetitions; i++) {
            builder.append(string);
        }
        return builder.toString();
    }

    /**
     * Checks if an index is within the bounds of a string
     * @param string The string to check against
     * @param index The index to check
     * @return <code>true</code> iff <code>string[index]</code> is inside the bounds of <code>string</code>.
     */
    public static boolean isWithinBounds(String string, int index) {
        return index > 0 && index < string.length();
    }

    /**
     * Checks if a character is a whitespace character
     * @param c The character to check
     * @return <code>true</code> iff <code>c</code> is a whitespace character.
     */
    public static boolean isWhitespaceCharacter(char c) {
        return WHITESPACE_CHARACTERS.indexOf(c) > -1;
    }

    /**
     * Retrieves the next non-whitespace character from a string with an offset
     * @param source The string to use
     * @param offset The initial offset
     * @return The first character after the offset that is not a whitespace character, or null
     * if not found.
     */
    public static Character nextNonWSChar(String source, int offset) {
        while (isWithinBounds(source, offset) && isWhitespaceCharacter(source.charAt(offset))) {
            offset++;
        }
        return isWithinBounds(source, offset) ? source.charAt(offset) : null;
    }
}
