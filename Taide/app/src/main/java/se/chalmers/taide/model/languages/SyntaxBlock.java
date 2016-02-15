package se.chalmers.taide.model.languages;

/**
 * Created by Matz on 2016-02-07.
 *
 * Interface for the syntax highlighting block data container.
 */
public interface SyntaxBlock {


    /**
     * Retrieve the start index of the block
     * @return The start index of the block
     */
    int getStartIndex();

    /**
     * Retrieve the end index of the block
     * @return The end index of the block
     */
    int getEndIndex();

    /**
     * Retrieve the color that should be assigned to this block
     * @return The resource ID of the color that should be assigned to this block
     */
    int getMarkupColor();
}
