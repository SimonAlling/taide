package se.chalmers.taide.model.languages;

import android.graphics.Color;

/**
 * Created by Matz on 2016-02-07.
 */
public interface SyntaxBlock {

    int getStartIndex();
    int getEndIndex();
    int getMarkupColor();
}
