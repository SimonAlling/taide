package se.chalmers.taide.util;

/**
 * Created by Matz on 2016-02-15.
 *
 * Utility class for handling the conversion between tabs and spaces.
 */
public class TabUtil {

    // Normal tab length of android is two spaces. This looks shitty.
    private static boolean useTabs = false;
    private static int nbrOfSpaces = 4;

    /**
     * Retrieves the string representing the given amount of "tabs". Exactly what
     * this consists of is derived from the useTabs and nbrOfSpaces variables. See
     * getters and setters to modify these parameters.
     * @param nbrOfTabs The number of "tabs" to generate
     * @return A representation of the given number of "tabs"
     */
    public static String getTabs(int nbrOfTabs){
        if(nbrOfTabs<=0){
            return "";
        }

        String result = "";
        if (nbrOfTabs > 0) {
            if (useTabs) {
                for (int i = 0; i < nbrOfTabs; i++) {
                    result += "\t";
                }
            } else {
                String tabs = "";
                for (int i = 0; i < nbrOfSpaces; i++) {
                    tabs += " ";
                }

                for (int i = 0; i < nbrOfTabs; i++) {
                    result += tabs;
                }
            }
        }

        return result;
    }

    /**
     * Set if the tab generator should depend upon spaces or tabs.
     * @param useTabs <code>true</code> for tabs, <code>false</code> for spaces
     */
    public static void setUseTabs(boolean useTabs){
        TabUtil.useTabs = useTabs;
    }

    /**
     * Retrives whether tabs or spaces is used
     * @return <code>true</code> if tabs are used, <code>false</code> if spaces are used.
     */
    public static boolean usesTabs(){
        return useTabs;
    }

    /**
     * Set the number of spaces to use to represent one tab.
     * NOTE: This does not affect at all unless the mode is set to use spaces
     * instead of tabs.
     * @param nbrOfSpaces The number of spaces to use. Must be > 0.
     */
    public static void setNbrOfSpaces(int nbrOfSpaces){
        TabUtil.nbrOfSpaces = Math.max(0, nbrOfSpaces);
    }

    /**
     * Retrieves the number of spaces that represent one tab
     * @return The number of spaces that represent one tab
     */
    public int getNbrOfSpaces(){
        return nbrOfSpaces;
    }
}
