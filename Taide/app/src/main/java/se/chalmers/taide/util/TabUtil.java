package se.chalmers.taide.util;

/**
 * Created by Matz on 2016-02-15.
 */
public class TabUtil {

    private static boolean useTabs = true;
    private static int nbrOfSpaces = 4;

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

    public static void setUseTabs(boolean useTabs){
        TabUtil.useTabs = useTabs;
    }

    public static boolean usesTabs(){
        return useTabs;
    }

    public static void setNbrOfSpaces(int nbrOfSpaces){
        TabUtil.nbrOfSpaces = nbrOfSpaces;
    }

    public int getNbrOfSpaces(){
        return nbrOfSpaces;
    }
}
