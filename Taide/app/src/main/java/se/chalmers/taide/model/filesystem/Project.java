package se.chalmers.taide.model.filesystem;

import java.io.File;

/**
 * Created by Matz on 2016-03-22.
 */
public interface Project {

    String getName();
    boolean setupProject();
    void loadData(FileSystem.OnProjectLoadListener listener);
    File getBaseFolder();
    CodeFile createFile(String folder, String name);
    CodeFile createDir(String folder, String name);
}
