package se.chalmers.taide.model.filesystem;

import android.content.Context;
import android.util.Log;

import se.chalmers.taide.model.ProjectType;
import se.chalmers.taide.model.filesystem.dropbox.DropboxProject;

/**
 * Created by Matz on 2016-03-11.
 */
public class FileSystemFactory {

    public static FileSystem getFileSystem(Context context){
        return new SimpleFileSystem(context);
    }

    protected static Project getProject(String name, ProjectType type){
        switch(type){
            case LOCAL_SYSTEM:  return new SimpleProject(name);
            case DROPBOX:       return new DropboxProject(name);
            default:            Log.w("FileSystemFactory", "Found an unknown project type: " + type);
                                return null;
        }
    }

    public static String getConcreteName(String rawName, ProjectType type){
        switch(type){
            case LOCAL_SYSTEM:  return rawName;
            case DROPBOX:       if(rawName.indexOf("/")!=rawName.lastIndexOf("/")){
                                    return rawName.substring(rawName.lastIndexOf("/", rawName.lastIndexOf("/")-1)+1, rawName.lastIndexOf("/"));
                                }else{
                                   return rawName;
                                }
            default:            return rawName;
        }
    }

}
