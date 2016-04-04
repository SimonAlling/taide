package se.chalmers.taide.model.filesystem.dropbox;

import android.util.Log;

import com.dropbox.client2.DropboxAPI;

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
    private RevisionHandler revisionHandler;

    public DropboxFile(File file, String syncLocation, RevisionHandler revisionHandler){
        super(file);
        this.file = file;
        this.syncLocation = syncLocation;
        this.revisionHandler = revisionHandler;
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
        Dropbox.syncFile(this.file, this.syncLocation, null);
    }

    @Override
    public boolean saveContents(String contents) {
        String prevContents = super.getContents();
        if(!prevContents.equals(contents)) {
            boolean success = super.saveContents(contents);
            if(success) {
                Dropbox.upload(this.file, this.syncLocation, revisionHandler==null?null:new Dropbox.OnActionDoneListener() {
                    @Override
                    public void onActionDone(boolean result) {
                        Dropbox.retrieveMetadata(syncLocation, new Dropbox.OnMetadataRetrieveListener() {
                            @Override
                            public void metadataRetrieved(DropboxAPI.Entry metadata) {
                                if(revisionHandler != null && metadata != null){
                                    revisionHandler.updateSingleEntry(metadata.parentPath()+metadata.fileName(), metadata.rev);
                                }
                            }
                        });
                    }
                });
            }
            return success;
        }else{
            Log.d("DropboxFile", "No change in file: Did not sync.");
            return true;
        }
    }

    @Override
    public boolean remove() {
        boolean success = super.remove();
        if(success){
            Dropbox.delete(this.syncLocation, null);
        }
        return success;
    }

    @Override
    public boolean rename(String newName) {
        boolean success = super.rename(newName);
        if(success){
            int pathIndex = syncLocation.lastIndexOf("/");
            String newPath = (pathIndex>=0?syncLocation.substring(0, pathIndex):"");
            Dropbox.rename(this.syncLocation, newPath, null);
        }
        return success;
    }


}
