package se.chalmers.taide.model.filesystem;

import android.content.Context;

/**
 * Created by Matz on 2016-03-11.
 */
public class FileSystemFactory {

    public static FileSystem getFileSystem(Context context){
        return new SimpleFileSystem(context);
    }

}
