package se.chalmers.taide.util;

/**
 * Created by Matz on 2016-03-07.
 */
public class StringUtil {

    /**
     * Retrieves the next non-whitespace character from a string with an offset
     * @param source The string to use
     * @param offset The initial offset
     * @return The first character after the offset that is not a whitespace character, or null
     * if not found.
     */
    public static Character nextNonWSChar(String source, int offset){
        while(offset>0 && source.length()>offset && (source.charAt(offset) == ' ' || source.charAt(offset) == '\t' || source.charAt(offset) == '\n')){
            offset++;
        }

        return (source.length()<=offset?null:source.charAt(offset));
    }
}
