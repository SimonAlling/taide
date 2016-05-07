package se.chalmers.taide.model.autofill;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matz on 2016-05-03.
 *
 * Class for handling reading/writing from/to data files for saving autofills data. The default
 * files should be provided as assets whilst new autofills can be added during runtime to the local
 * storage. There are also methods to reset an autofill category to its default again.
 *
 * Note that the term 'category' is used frequently in this class. This often refers to the programming
 * language, although the name 'category' is used so that the functionality is not limited to only
 * language names.
 */
public class AutoFillFactory {

    /* The files that will be used */
    private static final String DEFAULT_AUTOFILLS_FILE = "autofill_defaults.json";      /* Should be an asset */
    private static final String fileLocation = "AUTOFILL_DATA.json";                    /* Will be placed in local storage */

    /* The key names of the JSON data */
    private static final String ATTR_DEFAULTS_ROOT = "defaults";
    private static final String ATTR_DEFAULTS_LANGUAGE = "language";
    private static final String ATTR_DEFAULTS_LANGUAGE_DATA = "data";
    private static final String ATTR_NAME_TRIGGER = "trigger";
    private static final String ATTR_NAME_PRE = "pre";
    private static final String ATTR_NAME_POST = "post";
    private static final String ATTR_NAME_SHORTCUT = "shortcut";

    /* The list of listeners listening for a change of autofills */
    private static List<AutoFillDataChangeListener> dataChangeListeners = new ArrayList<>();

    /**
     * Retrieves the autofills specified in the savefile under the specified name
     * @param context The current application context
     * @param category The name of the category the requested autofills are denoted by
     * @return A list of autofills that are in the given category
     */
    public static void getAutofillsByCategory(Context context, String category, OnAutoFillsLoadedListener listener){
        File saveFile = new File(context.getFilesDir()+"/"+fileLocation);
        new AutoFillLoader(saveFile, category, listener).execute();
    }

    /**
     * Update the contents of any number of autofills and store to the savefile. This will also
     * notify any realtime autofillers connected to this class.
     * @param context The current application context
     * @param category The name of the category the autofills are ordered within
     * @param changes The changes to apply
     */
    public static void changeAutofillsData(Context context, final String category, AutoFillChange... changes){
        if(changes != null && changes.length>0){
            File saveFile = new File(context.getFilesDir()+"/"+fileLocation);
            new AutoFillDataChanger(saveFile, category).execute(changes);

            //Notify listeners
            for(AutoFillChange change : changes){
                if(change != null){
                    if(change.isAdd()){
                        for(AutoFillDataChangeListener listener : dataChangeListeners){
                            listener.autoFillAdded(category, change.getAutoFill());
                        }
                    }else {
                        for(AutoFillDataChangeListener listener : dataChangeListeners){
                            listener.autoFillRemoved(category, change.getAutoFill());
                        }
                    }
                }
            }
        }
    }

    /**
     * Resets a category/all categories to the default factory autofills. Use the flag forceOverwrite
     * to decide whether to respect any custom changes.
     * @param context The current application context
     * @param category The category to apply this to, or null for setting all
     * @param forceOverwrite <code>true</code> to overwrite any current values if existant, <code>false</code> otherwise
     */
    public static void resetToDefaultAutofills(Context context, String category, boolean forceOverwrite){
        try{
            File saveFile = new File(context.getFilesDir()+"/"+fileLocation);
            InputStream in = context.getAssets().open(DEFAULT_AUTOFILLS_FILE);
            new DefaultAutofillLoader(saveFile, category, forceOverwrite).execute(in);
        }catch(IOException ioe){
            Log.w("AutoFillFactory", "Could not load default autofills: "+ioe.getMessage());
        }
    }

    /**
     * Add a listener that listens for changes
     * @param listener The listener to add
     */
    public static void registerAutofillChangeListener(AutoFillDataChangeListener listener){
        if(listener != null){
            dataChangeListeners.add(listener);
        }
    }

    /**
     * Removes a listener so that it does not receive any more updates about changes
     * @param listener The listener to remove
     */
    public static void unregisterAutofillChangeListener(AutoFillDataChangeListener listener){
        dataChangeListeners.remove(listener);
    }


    /**
     * Simple class to contain data for a change of autofills, either an add or a remove.
     */
    public class AutoFillChange{
        private boolean add = true;
        private AutoFill autoFill;

        /**
         * Creates an autofillchange.
         * @param autoFill The autofill to use
         * @param add <code>true</code> if the autofill was added, <code>false if it was removed</code>
         */
        public AutoFillChange(AutoFill autoFill, boolean add){
            this.autoFill = autoFill;
            this.add = add;
        }
        public AutoFill getAutoFill(){return autoFill;}
        public boolean isAdd(){return add;}
    }

    /**
     * Class for changing the autofill data files. Assumes that the files follows the
     * appropiate JSON format.
     */
    protected static class AutoFillDataChanger extends AsyncTask<AutoFillChange, Void, Void>{
        private File saveFile;
        private String category;

        /**
         * Initiates an instance of this task
         * @param saveFile The file to save/load data from
         * @param category The category to write these changes to
         */
        public AutoFillDataChanger(File saveFile, String category){
            this.saveFile = saveFile;
            this.category = category;
        }

        /**
         * Executes the task
         * @param autoFillChanges The changes to apply for the given category
         * @return null. always.
         */
        public Void doInBackground(AutoFillChange... autoFillChanges){
            String content = getContent(saveFile);
            try {
                JSONObject obj;
                if(content != null){
                    obj = new JSONObject(content);
                }else{
                    obj = new JSONObject();
                }

                if(!obj.has(category)) {
                    //If the category does not exist, create it.
                    JSONArray data = new JSONArray();
                    obj.put(category, data);
                }

                JSONArray data = obj.getJSONArray(category);
                for(AutoFillChange autoFillChange : autoFillChanges){
                    if(autoFillChange.isAdd()){
                        data.put(getJSONObject(autoFillChange.getAutoFill()));
                    }else{
                        data = removeAutofillFromArray(data, autoFillChange.getAutoFill());
                    }
                }
                obj.put(category, data);        //If the reference to data has changed, store it

                BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                bw.write(obj.toString());
                bw.flush();
                bw.close();
            } catch(JSONException ex){
                Log.w("AutoFillFactory", "Could not read autofill save file: " + ex.getMessage());
                return null;
            } catch(IOException ioe){
                Log.w("AutoFillFactory", "Could not save autofill: "+ioe.getMessage());
            }

            return null;
        }

        /**
         * Creates a JSONObject from the given autofill containing all relevant data.
         * @param autoFill The autofill to create a JSONObject from
         * @return The JSONObject that describes the given autofill
         * @throws JSONException If anything goes wrong. Should never happen.
         */
        private static JSONObject getJSONObject(AutoFill autoFill) throws JSONException{
            JSONObject obj = new JSONObject();
            obj.put(ATTR_NAME_TRIGGER, autoFill.getTrigger());
            obj.put(ATTR_NAME_PRE, autoFill.getPrefixData());
            obj.put(ATTR_NAME_POST, autoFill.getSuffixData());
            if(autoFill.getSuffixedTrigger() == null){
                obj.put(ATTR_NAME_SHORTCUT, false);
            }
            return obj;
        }

        /**
         * Removes the given autofill from the given JSONArray. The removal is executed if any
         * item in the array has the same value on the key ATTR_NAME_TRIGGER as the autofill trigger.
         * @param data The array to act upon
         * @param autoFill The autofill to remove
         * @return The array with the autofill removed (if found)
         * @throws JSONException If anything goes wrong. This should not happen.
         */
        private static JSONArray removeAutofillFromArray(JSONArray data, AutoFill autoFill) throws JSONException{
            for(int i = 0; i<data.length(); i++){
                JSONObject obj = data.getJSONObject(i);
                if(obj.has(ATTR_NAME_TRIGGER) && obj.getString(ATTR_NAME_TRIGGER).equals(autoFill.getTrigger())){
                    return remove(i, data);
                }
            }
            return data;
        }

        /**
         * Removes the object at an index in a JSONArray
         * @param index The index of the object
         * @param from The array to remove from
         * @return The array with the given element removed.
         */
        public static JSONArray remove(final int index, final JSONArray from) {
            final List<JSONObject> objects = asList(from);
            objects.remove(index);

            final JSONArray ja = new JSONArray();
            for (final JSONObject obj : objects) {
                ja.put(obj);
            }

            return ja;
        }

        /**
         * Converts a JSONArray to a Java list of JSONObjects
         * @param ja The array to convert
         * @return A Java list of JSONObjects from the JSONArray
         */
        public static List<JSONObject> asList(final JSONArray ja) {
            final int len = ja.length();
            final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
            for (int i = 0; i < len; i++) {
                final JSONObject obj = ja.optJSONObject(i);
                if (obj != null) {
                    result.add(obj);
                }
            }
            return result;
        }
    }

    /**
     * Class for loading data from the default files to the given save file
     */
    protected static class DefaultAutofillLoader extends AsyncTask<InputStream, Void, Void>{

        private File saveFile;
        private String category;
        private boolean forceOverwrite;

        /**
         * Initiates an instance of this task
         * @param saveFile The file to save the data to
         * @param category The category to apply this to, or null for all categories
         * @param forceOverwrite <code>true</code> if any previous custom changes should be overwritten
         */
        public DefaultAutofillLoader(File saveFile, String category, boolean forceOverwrite){
            this.saveFile = saveFile;
            this.category = category;
            this.forceOverwrite = forceOverwrite;
        }

        /**
         * Executes the task
         * @param defaultFiles The default files to retrieve data from
         * @return null. always.
         */
        public Void doInBackground(InputStream... defaultFiles){
            if(defaultFiles != null && saveFile != null){
                String sourceContents = getContent(saveFile);
                try{
                    JSONObject sourceObject;
                    if(sourceContents != null){
                        sourceObject = new JSONObject(sourceContents);
                    }else{
                        sourceObject = new JSONObject();
                    }

                    for(InputStream in : defaultFiles){
                        if(in != null) {
                            String contents = getContent(in);
                            JSONObject obj = new JSONObject(contents);
                            if (obj.has(ATTR_DEFAULTS_ROOT)) {
                                JSONArray data = obj.getJSONArray(ATTR_DEFAULTS_ROOT);
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject currentObject = data.getJSONObject(i);
                                    if (currentObject.has(ATTR_DEFAULTS_LANGUAGE) && currentObject.has(ATTR_DEFAULTS_LANGUAGE_DATA)) {
                                        if(category == null || currentObject.getString(ATTR_DEFAULTS_LANGUAGE).equals(category)) {
                                            if (forceOverwrite || !sourceObject.has(currentObject.getString(ATTR_DEFAULTS_LANGUAGE))) {
                                                sourceObject.put(currentObject.getString(ATTR_DEFAULTS_LANGUAGE), currentObject.getJSONArray(ATTR_DEFAULTS_LANGUAGE_DATA));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                    bw.write(sourceObject.toString());
                    bw.flush();
                    bw.close();
                } catch(JSONException ex){
                    Log.w("AutoFillFactory", "Could not load default autofills: "+ex.getMessage());
                } catch(IOException ioe){
                    Log.w("AutoFillFactory", "Could not save default autofills to file: "+ioe.getMessage());
                }
            }
            return null;
        }
    }


    /**
     * Task for handling loading data from a category.
     */
    private static class AutoFillLoader extends AsyncTask<Void, Void, List<AutoFill>>{
        private File saveFile;
        private OnAutoFillsLoadedListener listener;
        private String category;

        /**
         * Initiates this task.
         * @param saveFile The file to load data from
         * @param category The category to load data from
         * @param listener The listener to be notified when the data has been retrieved
         */
        public AutoFillLoader(File saveFile, String category, OnAutoFillsLoadedListener listener){
            this.saveFile = saveFile;
            this.category = category;
            this.listener = listener;
        }

        /**
         * Execute the task
         * @param v Do not use.
         * @return The list of autofills loaded from the file. null if anything went wrong.
         */
        public List<AutoFill> doInBackground(Void... v){
            String content = getContent(saveFile);
            if(content != null) {
                try {
                    JSONObject obj = new JSONObject(content);
                    if(obj.has(category)){
                        List<AutoFill> autoFills = new ArrayList<>();
                        JSONArray data = obj.getJSONArray(category);
                        for(int i = 0; i<data.length(); i++){
                            addDataToList(autoFills, data.getJSONObject(i));
                        }
                        return autoFills;
                    } else {
                        Log.w("AutoFillFactory", "The category "+category+" was not found");
                    }
                } catch(JSONException ex){
                    Log.w("AutoFillFactory", "Could not read autofill save file: " + ex.getMessage());
                    return null;
                }
            }

            return null;
        }

        /**
         * Parses the given JSONObject to an autofill and adds it to the list, if it contains valid data.
         * @param autoFills The list to add to
         * @param obj The object to retrieve data from.
         * @throws JSONException If any errors were found. This should never happen (everything is checked in advance)
         */
        private static void addDataToList(List<AutoFill> autoFills, JSONObject obj) throws JSONException{
            String trigger = "", pre = "", post = "";
            if(obj.has(ATTR_NAME_TRIGGER)){             //Require trigger attribute
                trigger = obj.getString(ATTR_NAME_TRIGGER);
                if(obj.has(ATTR_NAME_PRE)){
                    pre = obj.getString(ATTR_NAME_PRE);
                }
                if(obj.has(ATTR_NAME_POST)){
                    post = obj.getString(ATTR_NAME_POST);
                }

                if(obj.has(ATTR_NAME_SHORTCUT) && !obj.getBoolean(ATTR_NAME_SHORTCUT)){
                    autoFills.add(new SimpleAutoFill(trigger, pre, post));
                }else {
                    autoFills.add(new ShortcutAutoFill(trigger, pre, post));
                }
            }
        }

        /**
         * Triggered when the task has finished executing. Notifies the listeners
         * @param autofills The autofills that were read from the load file.
         */
        public void onPostExecute(List<AutoFill> autofills){
            if(listener != null){
                listener.onAutoFillsLoaded(autofills);
            }
        }
    }

    /**
     * Retrieve the contents of the given file
     * @param f The file to read
     * @return The contents of the given file, or null if not found/invalid
     */
    private static String getContent(File f){
        if(f != null && f.exists()){
            try {
                return getContent(new BufferedInputStream(new FileInputStream(f)));
            }catch(IOException ioe){
                return null;
            }
        }
        return null;
    }

    /**
     * Retrieve the content of the given input stream
     * @param in The input stream to read
     * @return The contents of the given input strea, or null if invalid
     */
    private static String getContent(InputStream in){
        String json = null;
        try {
            if(in != null) {
                int size = in.available();
                byte[] buffer = new byte[size];
                in.read(buffer);
                in.close();
                json = new String(buffer, "UTF-8");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    /**
     * Interface for handling the event when autofills has been loaded from file
     */
    public interface OnAutoFillsLoadedListener{

        /**
         * Triggered when the autofills has been loaded
         * @param autofills The autofills that were found
         */
        void onAutoFillsLoaded(List<AutoFill> autofills);
    }

    /**
     * Interface for handling changes of autofills that goes through this class.
     */
    public interface AutoFillDataChangeListener{

        /**
         * Triggered when an autofill has been added
         * @param category The category it was added to
         * @param a The autofill that was added
         */
        void autoFillAdded(String category, AutoFill a);

        /**
         * Triggered when an autofill has been removed
         * @param category The category it was removed from
         * @param a The autofill that was removed
         */
        void autoFillRemoved(String category, AutoFill a);
    }
}
