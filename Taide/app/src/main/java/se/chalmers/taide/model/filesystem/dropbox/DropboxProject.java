package se.chalmers.taide.model.filesystem.dropbox;

import android.util.Log;

import com.dropbox.client2.DropboxAPI;

import java.io.File;
import java.io.IOException;

import se.chalmers.taide.model.ProjectType;
import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
import se.chalmers.taide.model.filesystem.FileSystemFactory;
import se.chalmers.taide.model.filesystem.SimpleProject;

/**
 * Created by Matz on 2016-03-22.
 */
public class DropboxProject extends SimpleProject {

    private String baseLink;
    private String localBaseFolder;
    private String dropboxBaseFolder;

    private int currentSyncCount = 0;
    private int currentFetchMetadata = 0;
    private boolean currentSyncSuccess = true;
    private RevisionHandler revisionHandler;

    public DropboxProject(String name){
        super(FileSystemFactory.getConcreteName(name, ProjectType.DROPBOX));
        this.baseLink = name;
    }

    @Override
    public void loadData(final FileSystem.OnProjectLoadListener listener) {
        this.localBaseFolder = getBaseFolder().getPath();
        this.dropboxBaseFolder = baseLink.substring(baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/") + 1) + 1) + 1) + 1, baseLink.lastIndexOf("/"));
        this.revisionHandler = RevisionHandler.registerHandler(this, dropboxBaseFolder);
        revisionHandler.loadRevisionState();

        Dropbox.retrieveMetadata(dropboxBaseFolder, new Dropbox.OnMetadataRetrieveListener() {
            @Override
            public void metadataRetrieved(DropboxAPI.Entry metadata) {
                syncEntry(metadata, new Dropbox.OnActionDoneListener() {
                    @Override
                    public void onActionDone(boolean result) {
                        revisionHandler.saveRevisionData();
                        if (listener != null) {
                            listener.projectLoaded(true);
                        }
                    }
                });
            }
        });
    }

    private void syncEntry(DropboxAPI.Entry entry, Dropbox.OnActionDoneListener listener){
        syncEntry("", entry, listener);
    }

    private void syncEntry(String path, DropboxAPI.Entry entry, final Dropbox.OnActionDoneListener listener){
        if(entry != null) {
            if (entry.isDir) {  //Folder.
                //Ignore all this stuff for base folder (already created and in path)
                if (!(entry.parentPath() + entry.fileName()).equalsIgnoreCase("/" + dropboxBaseFolder)) {
                    //Update folder structure var
                    path += (path.length() == 0 ? "" : "/") + entry.fileName();

                    //Create dir.
                    File dir = new File(localBaseFolder + "/" + path);
                    if (!dir.exists()) {
                        currentSyncSuccess &= dir.mkdir();
                    }
                }

                //Sync folder contents
                if (entry.contents != null) {
                    for (DropboxAPI.Entry e : entry.contents) {
                        if (e.isDir) {
                            currentFetchMetadata++;
                            final String childPath = path;
                            Dropbox.retrieveMetadata(e.parentPath() + e.fileName(), new Dropbox.OnMetadataRetrieveListener() {
                                @Override
                                public void metadataRetrieved(DropboxAPI.Entry metadata) {
                                    currentFetchMetadata--;
                                    syncEntry(childPath, metadata, listener);
                                }
                            });
                        } else {
                            syncEntry(path, e, listener);
                        }
                    }
                } else {
                    if (currentSyncCount == 0 && currentFetchMetadata == 0) {
                        if (listener != null) {
                            listener.onActionDone(currentSyncSuccess);
                        }
                        currentSyncSuccess = true;
                    }
                }
            } else {        //File
                if(revisionHandler.shouldSyncEntry(entry)) {
                    currentSyncCount++;
                    Dropbox.syncFile(new File(localBaseFolder + "/" + path + "/" + entry.fileName()), entry.parentPath() + entry.fileName(), new Dropbox.OnActionDoneListener() {
                        @Override
                        public void onActionDone(boolean result) {
                            currentSyncSuccess &= result;
                            if (--currentSyncCount == 0 && currentFetchMetadata == 0) {
                                if (listener != null) {
                                    listener.onActionDone(currentSyncSuccess);
                                }
                                //Reset success var
                                currentSyncSuccess = true;
                            }
                        }
                    });
                }
            }
        }
    }


    @Override
    public boolean setupProject() {
        boolean success = super.setupProject();
        this.dropboxBaseFolder = baseLink.substring(baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/")+1)+1)+1)+1, baseLink.lastIndexOf("/"));
        return success;
    }

    @Override
    public CodeFile createFile(String folder, String name) {
        if(!folder.startsWith(localBaseFolder)){
            throw new IllegalArgumentException("Cannot handle files outside project scope");
        }
        folder = folder.substring(localBaseFolder.length());

        File f = new File(localBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name);
        if(!f.exists()) {
            try {
                if (f.createNewFile()) {
                    DropboxFile file = new DropboxFile(f, dropboxBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name, revisionHandler);
                    file.saveContents(" ");      //Sync empty file.
                    return file;
                }
            } catch (IOException ioe) {
                Log.e("Project", "Could not create file: " + ioe.getMessage());
            }
        }else{
            return new DropboxFile(f, dropboxBaseFolder+"/"+folder+(folder.length()>0?"/":"")+name, revisionHandler);
        }

        return null;
    }

    @Override
    public CodeFile createDir(String folder, String name) {
        if(!folder.startsWith(localBaseFolder)){
            throw new IllegalArgumentException("Cannot handle directories outside project scope");
        }
        folder = folder.substring(localBaseFolder.length());

        File f = new File(localBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name);
        if(!f.exists()) {
            if (f.mkdir()) {
                DropboxFile dir = new DropboxFile(f, dropboxBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name, revisionHandler);
                dir.saveContents(" ");      //Sync empty dir.
                return dir;
            }
        }else{
            return new DropboxFile(f, dropboxBaseFolder+"/"+folder+(folder.length()>0?"/":"")+name, revisionHandler);
        }

        return null;
    }

    @Override
    public CodeFile getCodeFile(File f) {
        if(!f.getPath().startsWith(localBaseFolder)){
            throw new IllegalArgumentException("Cannot handle files outside project scope");
        }

        String syncLocation = dropboxBaseFolder+"/"+f.getPath().substring(localBaseFolder.length()+1);
        return new DropboxFile(f, syncLocation, revisionHandler);
    }
}
