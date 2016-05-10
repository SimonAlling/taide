package se.chalmers.taide.util;

import java.util.HashMap;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-15.
 *
 * Utility class for handling the conversion between tabs and spaces.
 */
public class TabUtil {

    private static final TabSetting DEFAULT_TABSETTING = new TabSetting(false, 4);
    private static HashMap<Class<? extends Language>, TabSetting> languageTabSettings = new HashMap<>();

    /**
     * Retrieves the string representing the given amount of "tabs". Exactly what
     * this consists of is derived from the useTabs and spacesPerTab variables. See
     * getters and setters to modify these parameters.
     *
     * A negative number of tabs is OK; it will give the same result as requesting zero tabs.
     * @param numberOfTabs The number of "tabs" to generate
     * @param lang The language to use settings from
     * @return A representation of the given number of "tabs"
     */
    public static String getTabs(int numberOfTabs, Language lang){
        TabSetting setting = getTabSettingByLanguage(lang);
        return getTabs(numberOfTabs, setting.usesTabs(), setting.getSpacesPerTab());
    }

    /**
     * Retrieves the string representing the given amount of "tabs". Exactly what
     * this consists of is derived from the useTabs and spacesPerTab variables. See
     * getters and setters to modify these parameters.
     *
     * A negative number of tabs is OK; it will give the same result as requesting zero tabs.
     * @param numberOfTabs The number of "tabs" to generate
     * @param useTabs If tabs should be used
     * @param spacesPerTab The number of spaces to use per tab (if spaces is used)
     * @return A representation of the given number of "tabs"
     */
    public static String getTabs(int numberOfTabs, boolean useTabs, int spacesPerTab) {
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
    public static void setTabSettingForLanguage(Language lang, boolean useTabs, int spacesPerTab) {
        if(lang != null) {
            languageTabSettings.put(lang.getClass(), new TabSetting(useTabs, spacesPerTab));
        }
    }

    /**
     * Checks whether a file of the given language should use tabs or spaces
     * @param lang The language to check the property for
     * @return <code>true</code> if the language uses tabs, <code>false</code> if it uses spaces
     */
    public static boolean usesTabs(Language lang){
        return getTabSettingByLanguage(lang).usesTabs();
    }

    /**
     * Retrieves how many spaces there are for each tab in the given language
     * @param lang The language to check the property for
     * @return The number of spaces per tab for the given language
     */
    public static int getSpacesPerTab(Language lang){
        return getTabSettingByLanguage(lang).getSpacesPerTab();
    }

    /**
     * Retrives whether tabs or spaces is used
     * @return <code>true</code> if tabs are used, <code>false</code> if spaces are used.
     */
    private static TabSetting getTabSettingByLanguage(Language lang) {
        if(lang != null && languageTabSettings.containsKey(lang.getClass())){
            return languageTabSettings.get(lang.getClass());
        }else{
            return DEFAULT_TABSETTING;
        }
    }


    /**
     * Simple class for containing the data for a setting concerning the tabs.
     *  * If ttabs or spaces is used
     *  * How many spaces there are for each tab
     */
    private static class TabSetting{
        private boolean useTabs = false;
        private int spacesPerTab = 4;
        public TabSetting(boolean useTabs, int spacesPerTab){
            this.useTabs = useTabs;
            this.spacesPerTab = spacesPerTab;
        }
        public boolean usesTabs(){return useTabs;}
        public int getSpacesPerTab(){return spacesPerTab;}
    }
}
