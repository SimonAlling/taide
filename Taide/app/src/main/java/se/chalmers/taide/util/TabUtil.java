package se.chalmers.taide.util;

/**
 * Created by Matz on 2016-02-15.
 *
 * Utility class for handling the conversion between tabs and spaces.
 */
public class TabUtil {

    // Normal tab length of android is two spaces. This looks shitty.
    private static boolean useTabs = false;
    private static int spacesPerTab = 4;

    /**
     * Retrieves the string representing the given amount of "tabs". Exactly what
     * this consists of is derived from the useTabs and spacesPerTab variables. See
     * getters and setters to modify these parameters.
     *
     * A negative number of tabs is OK; it will give the same result as requesting zero tabs.
     * @param numberOfTabs The number of "tabs" to generate
     * @return A representation of the given number of "tabs"
     */
    public static String getTabs(int numberOfTabs) {
        if (useTabs) {
            return StringUtil.repeat("\t", numberOfTabs);
        } else {
            return StringUtil.repeat(" ", numberOfTabs * spacesPerTab);
        }
    }

    /**
     * Set if the tab generator should depend upon spaces or tabs.
     * @param useTabs <code>true</code> for tabs, <code>false</code> for spaces
     */
    public static void setUseTabs(boolean useTabs) {
        TabUtil.useTabs = useTabs;
    }

    /**
     * Retrives whether tabs or spaces is used
     * @return <code>true</code> if tabs are used, <code>false</code> if spaces are used.
     */
    public static boolean usesTabs() {
        return useTabs;
    }

    /**
     * Set the number of spaces to use to represent one tab.
     * NOTE: This does not affect at all unless the mode is set to use spaces
     * instead of tabs.
     * @param spacesPerTab The number of spaces to use. Must be > 0.
     */
    public static void setSpacesPerTab(int spacesPerTab) {
        TabUtil.spacesPerTab = Math.max(0, spacesPerTab);
    }

    /**
     * Retrieves the number of spaces that represent one tab
     * @return The number of spaces that represent one tab
     */
    public int getSpacesPerTab() {
        return spacesPerTab;
    }
}
