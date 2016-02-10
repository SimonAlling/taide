package se.chalmers.taide.model.languages;

/**
 * Created by Matz on 2016-02-07.
 */
public class SimpleSyntaxBlock implements SyntaxBlock{

    private int startIndex, endIndex;
    private int color;

    protected SimpleSyntaxBlock(int startIndex, int endIndex, int color){
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.color = color;
    }

    public int getStartIndex(){
        return startIndex;
    }

    public int getEndIndex(){
        return endIndex;
    }

    public int getMarkupColor(){
        return color;
    }

    @Override
    public String toString(){
        return "SimpleSyntaxBlock {startIndex:"+startIndex+", endIndex:"+endIndex+", color:"+color+"}";
    }
}
