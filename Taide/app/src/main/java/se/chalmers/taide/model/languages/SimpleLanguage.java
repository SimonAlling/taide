package se.chalmers.taide.model.languages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matz on 2016-02-15.
 */
public abstract class SimpleLanguage implements Language {

    @Override
    public List<SyntaxBlock> getSyntaxBlocks(String sourceCode) {
        return new ArrayList<>();
    }

    @Override
    public String getIndentationPrefix(String source, int start, String line) {
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

    @Override
    public String getIndentationSuffix(String source, int start, String lines) {
        return "";
    }
}
