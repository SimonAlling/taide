package se.chalmers.taide.model.languages;

/**
 * Created by Matz on 2016-02-07.
 *
 * Data container for a highlight syntax block.
 */
public class SimpleSyntaxBlock implements SyntaxBlock {

    private int startIndex, endIndex;
    private int color;
    private boolean bold, italic;

    protected SimpleSyntaxBlock(int startIndex, int endIndex, int color){
        this(startIndex, endIndex, color, false, false);
    }

    protected SimpleSyntaxBlock(int startIndex, int endIndex, int color, boolean bold, boolean italic){
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
    }

    /**
     * Retrieve the start index of the block
     * @return The start index of the block
     */
    public int getStartIndex(){
        return startIndex;
    }

    /**
     * Retrieve the end index of the block
     * @return The end index of the block
     */
    public int getEndIndex(){
        return endIndex;
    }

    /**
     * Retrieve the color that should be assigned to this block
     * @return The resource ID of the color that should be assigned to this block
     */
    public int getMarkupColor(){
        return color;
    }

    /**
     * Checks whether the block should be formatted bold.
     * @return <code>true</code> if the block is bold, <code>false</code> otherwise
     */
    public boolean isBold(){
        return bold;
    }

    /**
     * Checks whether the block should be formatted italic.
     * @return <code>true</code> if the block is italic, <code>false</code> otherwise
     */
    public boolean isItalic(){
        return italic;
    }

    @Override
    public String toString(){
        return "SimpleSyntaxBlock {startIndex:"+startIndex+", endIndex:"+endIndex+", color:"+color+"}";
    }
}
