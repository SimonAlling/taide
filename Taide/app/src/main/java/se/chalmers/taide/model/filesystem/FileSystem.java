package se.chalmers.taide.model.filesystem;

import java.util.List;

import se.chalmers.taide.model.ProjectType;

/**
 * Created by Matz on 2016-03-11.
 */
public interface FileSystem {

    boolean newProject(String projectName, ProjectType type, OnProjectLoadListener listener);
    boolean setProject(String projectName, OnProjectLoadListener listener);
    List<String> getExistingProjects();

    CodeFile getCurrentDir();
    List<CodeFile> getFilesInCurrentDir();
    CodeFile findFileByName(String name);
    String getCurrentProject();

    CodeFile createFile(String name);
    CodeFile createDir(String name);
    void saveFile(CodeFile file, String contents);

    boolean stepUpOneLevel();
    boolean stepIntoDir(CodeFile dir);
    boolean canStepUpOneLevel();

    interface OnProjectLoadListener{
        void projectLoaded(boolean success);
    }
}
