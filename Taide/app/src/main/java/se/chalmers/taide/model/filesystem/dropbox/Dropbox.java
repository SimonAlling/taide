package se.chalmers.taide.model.filesystem.dropbox;

import android.os.DropBoxManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;

import java.io.File;

/**
 * Created by Matz on 2016-03-26.
 */
public class Dropbox {

    private static DropboxAPI<?> api;

    private static void checkInit(){
        if(api == null){
            api = DropboxFactory.getAPI();
            if(api == null) {
                throw new IllegalStateException("Trying to make calls before calling DropboxFactory.initDropboxIntegration(Context)");
            }
        }else if(!api.getSession().isLinked()){
            throw new IllegalStateException("Invalid dropbox session: Not linked");
        }
    }

    public static void syncFile(File file, String syncLocation){
        checkInit();
        Log.d("Dropbox", "Performing sync of file: '"+syncLocation+"'");
        Syncer syncer = new Syncer(api, syncLocation, file);
        syncer.execute();
    }

    public static void upload(File file, String syncLocation){
        checkInit();
        Log.d("Dropbox", "Uploading file: '" + syncLocation + "'");
        Uploader uploader = new Uploader(api, syncLocation, file);
        uploader.execute();
    }

    public static void delete(String path){
        checkInit();
        Log.d("Dropbox", "Deleting file: '" + path + "'");
        Deleter deleter = new Deleter(api, path);
        deleter.execute();
    }

    public static void rename(String path, String newPath){
        Log.d("Dropbox", "Renaming file (via move call): '" + path + "' to '"+newPath+"'");
        move(path, newPath);
    }

    public static void move(String path, String newPath){
        checkInit();
        Log.d("Dropbox", "Moving file: '" + path + "' to '"+newPath+"'");
        Mover mover = new Mover(api, path, newPath);
        mover.execute();
    }

    public static void retrieveMetadata(String path, OnMetadataRetrieveListener listener){
        checkInit();
        Log.d("Dropbox", "Requesting meta data for: '" + path + "'");
        MetadataRetriever retriever = new MetadataRetriever(api, path, listener);
        retriever.execute();
    }

    public interface OnMetadataRetrieveListener{
        void metadataRetrieved(DropboxAPI.Entry metadata);
    }
}
