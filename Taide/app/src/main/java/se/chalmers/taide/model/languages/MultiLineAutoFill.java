package se.chalmers.taide.model.languages;

import se.chalmers.taide.model.AutoFill;
import se.chalmers.taide.util.StringUtil;

/**
 * Created by Matz on 2016-02-29.
 *
 * An advanced variant of the auto fill which allows both indentation and
 * generic multiple lines. This makes it possible to have dynamic auto fills
 * in some extent.
 * It also has support for ignoring the auto fill functionality in comments.
 */
public class MultiLineAutoFill implements AutoFill {

    public static final String INDENT_TABS = "MultiLine:IndentTabs";

    private String trigger;
    private String[] prefix, suffix;
    private Language lang;
    private boolean applyInComments;

    /**
     * Creates an auto fill object with the given params
     * @param lang The language to use (used for indentation)
     * @param trigger The trigger to use
     * @param applyInComments If the auto fill should be used for comments as well.
     */
    public MultiLineAutoFill(Language lang, String trigger, boolean applyInComments) {
        this.lang = lang;
        this.trigger = trigger;
        this.applyInComments = applyInComments;
    }

    /**
     * Retrieves the trigger to which this auto fill should react
     * @return The trigger text that should fire an event
     */
    @Override
    public String getTrigger() {
        return trigger;
    }

    @Override
    public String getTriggerSuffix() { return null; }

    @Override
    public String getSuffixedTrigger() { return getTrigger() + StringUtil.emptyIfNull(getTriggerSuffix()); }

    /**
     * Retrieves the text that should be placed before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed before the selection marker
     */
    @Override
    public String getPrefix(String source, int index) {
        return getStringFromPartArray(source, index, prefix);
    }

    /**
     * Retrieves the text that should be after before the selection marker
     * @param source The entire text that triggered this call
     * @param index The index of the selection marker
     * @return The text that should be placed after the selection marker
     */
    @Override
    public String getSuffix(String source, int index) {
        return getStringFromPartArray(source, index, suffix);
    }

    /**
     * Retrieves the number of extra chars (forward) that should be considered as well
     * in the replacement algorithm.
     * @param source The source code
     * @param offset The offset of the current selection
     * @return The number of extra chars to be considered in the replacement
     */
    @Override
    public int selectionIncreaseCount(String source, int offset){
        return 0;
    }

    /**
     * Generates a single string from an array that is constructed in the way
     * that setPrefix() and setSuffix() demands. New line characters will be
     * generated between each string element, unless the string equals the global
     * INDENT_TABS variable. Then the proper amount of tabs will be generated
     * instead, without an ending new line character.
     * @param source The text that generated this auto fill event
     * @param index The index of the selection marker in the text
     * @param array The array of string with data to use
     * @return A single string formatted as the text above, based on the array contents
     */
    private String getStringFromPartArray(String source, int index, String[] array) {
        if (!applyInComments && lang.isInComment(source, index)) {
            return "";
        }

        StringBuffer sourceBuffer = new StringBuffer(source);
        StringBuffer b = new StringBuffer();
        for (int i = 0; i<array.length; i++) {
            if (array[i].equals(INDENT_TABS)) {
                // Determine text
                String currentSource = sourceBuffer.toString();
                int nextNewLine = currentSource.indexOf("\n", index);
                String prevLine = currentSource.substring(Math.max(0, currentSource.lastIndexOf("\n", index - 2) + 1), nextNewLine < 0 ? currentSource.length() : nextNewLine-1);
                String tab = lang.getIndentationPrefix(currentSource, index, prevLine);

                // Add text
                b.append(tab);
                sourceBuffer.insert(index, tab);
                index += tab.length();
            } else {
                // Add text
                b.append(array[i]);
                sourceBuffer.insert(index, array[i]);
                index += array[i].length();

                // Add newline (if not last item)
                if (i < array.length-1) {
                    b.append("\n");
                    sourceBuffer.insert(index, "\n");
                    index += 1;
                }
            }
        }

        return b.toString();
    }

    /**
     * Sets the value of the prefix. This will override all previous
     * set calls of this object.
     * NOTE: To generate default indentation, use MultiLineAutoFill.INDENT_TABS.
     * This will generate the correct indentation and will NOT end the line
     * automatically.
     * @param prefixes The prefix, separated per line
     * @return This object, to make it easier to chain calls.
     */
    public MultiLineAutoFill setPrefix(String... prefixes) {
        this.prefix = prefixes;
        return this;
    }


    /**
     * Sets the value of the suffix. This will override all previous
     * set calls of this object.
     * NOTE: To generate default indentation, use MultiLineAutoFill.INDENT_TABS.
     * This will generate the correct indentation and will NOT end the line
     * automatically.
     * @param suffixes The suffix, separated per line
     * @return This object, to make it easier to chain calls.
     */
    public MultiLineAutoFill setSuffix(String... suffixes) {
        this.suffix = suffixes;
        return this;
    }
}
