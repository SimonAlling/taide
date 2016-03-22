package se.chalmers.taide.model.filesystem;

import android.content.Context;
import android.util.Log;

import se.chalmers.taide.model.ProjectType;

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
            default:            Log.w("FileSystemFactory", "Found an unknown project type: " + type);
                                return null;
        }
    }

}
