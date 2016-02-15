package se.chalmers.taide.model.languages;

import java.util.List;

/**
 * Created by Matz on 2016-02-07.
 */
public interface Language {

    String getName();

    List<SyntaxBlock> getSyntaxBlocks(String sourceCode);
    String getIndentationPrefix(String source, int start, String line);
    String getIndentationSuffix(String source, int start, String line);

}
