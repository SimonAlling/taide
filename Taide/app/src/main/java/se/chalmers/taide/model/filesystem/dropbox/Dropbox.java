package se.chalmers.taide.model.filesystem.dropbox;

import android.os.DropBoxManager;

import com.dropbox.client2.DropboxAPI;

import java.io.File;

/**
 * Created by Matz on 2016-03-26.
 */
public class Dropbox {

    private static DropboxAPI<?> api;

    public static void init(DropboxAPI<?> api){
        Dropbox.api = api;
    }

    private static void checkInit(){
        if(api == null){
            throw new IllegalStateException("Trying to make calls before calling init in "+Dropbox.class.getCanonicalName());
        }else if(!api.getSession().isLinked()){
            throw new IllegalStateException("Invalid dropbox session: Not linked");
        }
    }

    public static void syncFile(File file, String syncLocation){
        checkInit();
        Syncer syncer = new Syncer(api, syncLocation, file);
        syncer.execute();
    }

    public static void upload(File file, String syncLocation){
        checkInit();
        Uploader uploader = new Uploader(api, syncLocation, file);
        uploader.execute();
    }

    public static void delete(String path){
        checkInit();
        Deleter deleter = new Deleter(api, path);
        deleter.execute();
    }

    public static void rename(String path, String newPath){
        move(path, newPath);
    }

    public static void move(String path, String newPath){
        checkInit();
        Mover mover = new Mover(api, path, newPath);
        mover.execute();
    }

    public static void retrieveMetadata(String path, OnMetadataRetrieveListener listener){
        checkInit();
        MetadataRetriever retriever = new MetadataRetriever(api, path, listener);
        retriever.execute();
    }

    public interface OnMetadataRetrieveListener{
        void metadataRetrieved(DropboxAPI.Entry metadata);
    }
}
