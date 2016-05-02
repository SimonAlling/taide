package se.chalmers.taide.util;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matz on 2016-05-02.
 *
 * Acts as a wrapper for parsing XML documents. The wanted elements are selected and
 * returned in a list of hashmaps.
 */
public class XMLParser {

    public static final String DEFAULT_VALUE_DIVIDER_STRING = "#";
    public static final String DEFAULT_VALUE_ELEMENT_SEPARATOR = ".";

    /**
     * Retrieves the wanted properties from a xml source file. The given root element will be the top one
     * for the search, and all properties are expected to be found within this element. Note that no exception
     * will be thrown if this is not the case, instead default values will be used.
     * Example:
     * <city>
     *     <house>
     *         <owner>Olle Persson</owner>
     *         <address>
     *             <street>Villagatan</street>
     *             <number>1</number>
     *         </address>
     *         <color>Red</color>
     *     </house>
     *     <house>
     *         <owner>Eva Andersson</owner>
     *         <address>
     *             <street>Villagatan</street>
     *             <number>5</number>
     *         </address>
     *         <color>Black</color>
     *     </house>
     * </city>
     *
     * From this structure, we could request the owner and adress of each house by calling:
     *   getValuesFromFile(**file**, "house", new String[]{"owner#UnknownOwner", "address.street#NoStreet", "address.number#-1"}
     *
     * @param xmlFile The xml file to use
     * @param rootElementName The tag name of the element to start search in (the "root" of the search)
     * @param properties The properties to search for. Each level of tags should be separated with the
     *                   DEFAULT_VALUE_ELEMENT_SEPARATOR variable. If the property has a default value,
     *                   declare this after the DEFAULT_VALUE_DIVIDER_STRING. Example: value#defaultValue.
     * @return A list of hashmaps, with every hashmap containing the properties that was requested.
     */
    public static List<HashMap<String, String>> getValuesFromXml(File xmlFile, String rootElementName, String[] properties){
        List<HashMap<String, String>> data = new LinkedList<>();

        //Convert into pattern objects
        Pattern[] propertyPatterns = new Pattern[properties.length];
        for(int i = 0; i<properties.length; i++){
            propertyPatterns[i] = new Pattern(properties[i]);
        }

        //Initiate search
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new FileInputStream(xmlFile), null);
            while(parser.next() != XmlPullParser.END_TAG){
                String name = parser.getName();
                if(name != null){
                    if(name.equals(rootElementName)){
                        HashMap<String, String> map = getXMLValues(parser, propertyPatterns);
                        data.add(map);
                    }
                }
            }
            return data;
        } catch(XmlPullParserException xppe){
            Log.e("XMLParser", "Invalid xml document ["+xmlFile.getName()+"]: "+xppe.getMessage());
        } catch(IOException ioe){
            Log.e("XMLParser", "Invalid xml file: "+xmlFile.getName());
        }

        return null;
    }

    /**
     * Retrieves the actual values of the requested properties. It is assumed that all of these properties
     * exist in the current element of the given parser, but no exception is raised if it does not.
     * @param parser The parser to use, the current element should contain all requested properties
     * @param patterns The patterns of the properties to find
     * @return A map of the vales of the properties mapped as property => value.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static HashMap<String, String> getXMLValues(XmlPullParser parser, Pattern[] patterns) throws XmlPullParserException, IOException{
        if(parser.getEventType() != XmlPullParser.START_TAG){
            return null;
        }

        HashMap<String, String> data = new HashMap<>();
        String currentPathName = "";
        int level = 1;
        //Compute all values
        do{
            switch(parser.next()){
                case XmlPullParser.START_TAG:   level++;
                                                currentPathName += (currentPathName.length()>0?DEFAULT_VALUE_ELEMENT_SEPARATOR:"")+parser.getName();break;
                case XmlPullParser.END_TAG:     level--;
                                                int removeIndex = currentPathName.lastIndexOf(DEFAULT_VALUE_ELEMENT_SEPARATOR);
                                                currentPathName = (removeIndex>=0?currentPathName.substring(0, removeIndex):"");break;
            }

            if(parser.getEventType() == XmlPullParser.START_TAG) {
                Pattern matchingPattern = getMatchingPattern(currentPathName, patterns);
                if (level > 0 && matchingPattern != null) {
                    parser.next();      //Go into the tag to fetch string value
                    if (parser.getEventType() == XmlPullParser.TEXT) {
                        data.put(matchingPattern.getName(), parser.getText());
                    }
                }
            }
        }while(level > 0);

        //Insert default values for values that has not been found
        for(Pattern pattern : patterns){
            if(!data.containsKey(pattern.getName())){
                data.put(pattern.getName(), pattern.getDefaultValue());
            }
        }

        return data;
    }

    /**
     * Checks if any of the patterns matches the name. If so, it returns the pattern that does.
     * @param name The name to check
     * @param patterns The available patterns
     * @return The pattern that matches the name, or null if none does.
     */
    private static Pattern getMatchingPattern(String name, Pattern[] patterns){
        for(Pattern pattern : patterns){
            if(pattern.getName().equalsIgnoreCase(name)){
                return pattern;
            }
        }

        return null;
    }

    /**
     * Class for handling a pattern (A search string with a default value). The pattern
     * can be instantiated by a string, by dividing the value and default value parts with
     * the value given by DEFAULT_VALUE_DIVIDER_STRING.
     */
    private static class Pattern{
        private String name;
        private String defaultValue;
        public Pattern(String pattern){
            if(pattern.contains(DEFAULT_VALUE_DIVIDER_STRING)){
                name = pattern.substring(0, pattern.indexOf(DEFAULT_VALUE_DIVIDER_STRING));
                defaultValue = pattern.substring(pattern.indexOf(DEFAULT_VALUE_DIVIDER_STRING)+DEFAULT_VALUE_DIVIDER_STRING.length());
            }else{
                name = pattern;
            }
        }

        public String getName(){
            return name;
        }

        public String getDefaultValue(){
            return defaultValue;
        }
    }
}
