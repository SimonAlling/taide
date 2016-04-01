package se.chalmers.taide.model.filesystem.dropbox;

import android.provider.SyncStateContract;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;

import java.io.File;
import java.io.IOException;

import se.chalmers.taide.model.ProjectType;
import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
import se.chalmers.taide.model.filesystem.FileSystemFactory;
import se.chalmers.taide.model.filesystem.SimpleCodeFile;
import se.chalmers.taide.model.filesystem.SimpleProject;

/**
 * Created by Matz on 2016-03-22.
 */
public class DropboxProject extends SimpleProject {

    private String baseLink;
    private String localBaseFolder;
    private String dropboxBaseFolder;

    public DropboxProject(String name){
        super(FileSystemFactory.getConcreteName(name, ProjectType.DROPBOX));
        this.baseLink = name;
    }

    @Override
    public void loadData(FileSystem.OnProjectLoadListener listener) {
        this.localBaseFolder = getBaseFolder().getPath();
        this.dropboxBaseFolder = baseLink.substring(baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/", baseLink.indexOf("/") + 1) + 1) + 1) + 1, baseLink.lastIndexOf("/"));
        //TODO sync all files
        if(listener != null){
            listener.projectLoaded(true);
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
                    DropboxFile file = new DropboxFile(f, dropboxBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name);
                    file.saveContents("");      //Sync empty file.
                    return file;
                }
            } catch (IOException ioe) {
                Log.e("Project", "Could not create file: " + ioe.getMessage());
            }
        }else{
            return new DropboxFile(f, dropboxBaseFolder+"/"+folder+(folder.length()>0?"/":"")+name);
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
                DropboxFile dir = new DropboxFile(f, dropboxBaseFolder+"/"+folder + (folder.length()>0?"/":"") + name);
                dir.saveContents("");      //Sync empty dir.
                return dir;
            }
        }else{
            return new DropboxFile(f, dropboxBaseFolder+"/"+folder+(folder.length()>0?"/":"")+name);
        }

        return null;
    }

    @Override
    public CodeFile getCodeFile(File f) {
        if(!f.getPath().startsWith(localBaseFolder)){
            throw new IllegalArgumentException("Cannot handle files outside project scope");
        }

        String syncLocation = dropboxBaseFolder+"/"+f.getPath().substring(localBaseFolder.length()+1);
        return new DropboxFile(f, syncLocation);
    }
}
