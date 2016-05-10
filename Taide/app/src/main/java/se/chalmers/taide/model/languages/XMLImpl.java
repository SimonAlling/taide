package se.chalmers.taide.model.languages;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.R;
import se.chalmers.taide.model.autofill.AbstractAutoFill;
import se.chalmers.taide.model.autofill.AutoFill;
import se.chalmers.taide.util.StringUtil;
import se.chalmers.taide.util.TabUtil;

/**
 * Created by Matz on 2016-05-09.
 */
public class XMLImpl extends SimpleLanguage {

    public static final String NAME = "XML";
    public static final String[] FILENAME_EXTENSIONS = {"xml"};
    public static final String[] SYNTAX_HIGHLIGHTING_TRIGGERS = {" ", "\n", "<", ">", "\""};

    private int[] colors;

    protected XMLImpl(Resources resources) {
        super(NAME, FILENAME_EXTENSIONS, SYNTAX_HIGHLIGHTING_TRIGGERS);
        //Init syntax highlightning colors.
        colors = resources.getIntArray(R.array.xml_syntax_default_colors);
    }

    @Override
    public String getDefaultContent(String filename) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    }

    @Override
    public String getIndentationPrefix(String source, int start, String line) {
        String basePrefix = super.getIndentationPrefix(source, start, line);
        if(shouldIndentExtra(source, start)){
            basePrefix += TabUtil.getTabs(1, this);
        }

        return basePrefix;
    }

    @Override
    public String getIndentationSuffix(String source, int start, String line) {
        String baseSuffix = super.getIndentationSuffix(source, start, line);
        int nextLine = getPositionOfNextLinebreak(source, start);
        if(shouldIndentExtra(source, start) && nextLine<source.length()-1 && source.charAt(nextLine+1) != '\n'){
            baseSuffix = "\n"+super.getIndentationPrefix(source, start, line);
        }

        return baseSuffix;
    }

    private boolean shouldIndentExtra(String source, int lineStart){
        int pos = getPositionOfNextLinebreak(source, lineStart);
        if(source.charAt(pos - 1) == '>') {
            int startTag = source.lastIndexOf("<", pos);
            return startTag >= 0 && source.charAt(startTag + 1) != '/';
        }else{
            return false;
        }
    }

    private int getPositionOfNextLinebreak(String source, int lineStart){
        int pos = source.indexOf("\n", lineStart);
        return pos<0 ? source.length() : pos;
    }

    @Override
    public List<SyntaxBlock> getSyntaxBlocks(String sourceCode) {
        List<SyntaxBlock> blocks = new ArrayList<>();

        for(int i = 0; i<sourceCode.length(); i++){
            if(sourceCode.charAt(i) == '<'){
                int nextTagEnd = sourceCode.indexOf('>', i+1);
                int nextSpace = sourceCode.indexOf(' ', i+1);
                int syntaxBlockEnd = Math.min(nextTagEnd<0?sourceCode.length():nextTagEnd+1, nextSpace<0?sourceCode.length():nextSpace+1);
                blocks.add(new SimpleSyntaxBlock(i+1+(Character.isLetter(sourceCode.charAt(i+1))?0:1), syntaxBlockEnd-1, colors[0]));
            }else if(sourceCode.charAt(i) == '\"'){
                int nextQuoteMark = sourceCode.indexOf('\"', i+1);
                if(nextQuoteMark<0){
                    blocks.add(new SimpleSyntaxBlock(i, sourceCode.length(), colors[1]));
                    break;
                }else{
                    blocks.add(new SimpleSyntaxBlock(i, nextQuoteMark+1, colors[1]));
                    i = nextQuoteMark;
                }
            }else if(sourceCode.charAt(i) == '='){
                int start = Math.max(0, sourceCode.lastIndexOf(" ", i));
                blocks.add(new SimpleSyntaxBlock(start, i, colors[2]));
            }
        }

        return blocks;
    }

    @Override
    public List<AutoFill> getAutoFills() {
        List<AutoFill> list = new ArrayList<>();
        list.add(new XMLTagAutofill());
        return list;
    }



    private class XMLTagAutofill extends AbstractAutoFill{

        public XMLTagAutofill() {
            super(">");
        }

        @Override
        public String getTriggerSuffix() {
            return null;
        }

        @Override
        public String getSuffixedTrigger() {
            return getTrigger() + StringUtil.emptyIfNull(getTriggerSuffix());
        }

        @Override
        public String getPrefix(String source, int index) {
            return ">";
        }

        @Override
        public String getSuffix(String source, int index) {
            if(index>0 && source.charAt(index-1) == '>'){
                int startIndex = source.lastIndexOf('<', index-1);
                if(startIndex>=0) {
                    String tag = source.substring(startIndex, index - 1);
                    if (!tag.contains("/")) {
                        return "</" + tag.substring(1, tag.length()) + ">";
                    }
                }
            }

            return "";
        }

        @Override
        public int selectionIncreaseCount(String source, int offset) {
            return 0;
        }
    }
}
