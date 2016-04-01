package se.chalmers.taide.model.filesystem.dropbox;

import android.util.Log;

import java.io.File;

import se.chalmers.taide.model.filesystem.SimpleCodeFile;

/**
 * Created by Matz on 2016-03-22.
 */
public class DropboxFile extends SimpleCodeFile{

    //Syncs every minute (if file explicitly loaded)
    private static final long SYNC_LIMIT = 60000L;

    private File file;
    private String syncLocation;
    private long lastSync = -1;

    public DropboxFile(File file, String syncLocation){
        super(file);
        this.file = file;
        this.syncLocation = syncLocation;
    }

    @Override
    public String getContents(){
        if(System.currentTimeMillis()-lastSync > SYNC_LIMIT){
            syncContent();
        }

        return super.getContents();
    }

    protected void syncContent(){
        lastSync = System.currentTimeMillis();
        Dropbox.syncFile(this.file, this.syncLocation);
    }

    @Override
    public boolean saveContents(String contents) {
        boolean success = super.saveContents(contents);
        Log.d("DBFile", "DROPBOX FILE");
        if(success) {
            Dropbox.upload(this.file, this.syncLocation);
        }
        return success;
    }

    @Override
    public boolean remove() {
        boolean success = super.remove();
        if(success){
            Dropbox.delete(this.syncLocation);
        }
        return success;
    }

    @Override
    public boolean rename(String newName) {
        boolean success = super.rename(newName);
        if(success){
            int pathIndex = syncLocation.lastIndexOf("/");
            String newPath = (pathIndex>=0?syncLocation.substring(0, pathIndex):"");
            Dropbox.rename(this.syncLocation, newPath);
        }
        return success;
    }


}
