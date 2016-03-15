package se.chalmers.taide.model.filesystem;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Matz on 2016-03-11.
 */
public class SimpleFileSystem implements FileSystem{

    private Context context;
    private String PROJECT_DIR = "";

    private File baseDir;
    private File currentDir;

    protected SimpleFileSystem(Context context){
        this.context = context;
        PROJECT_DIR = context.getFilesDir().getAbsolutePath();
    }

    @Override
    public boolean newProject(String projectName){
        if(getExistingProjects().contains(projectName)){
            return true;
        }

        try {
            File projectFile = getProjectFile(projectName);
            Log.d("FileSystem", "Creating folders: " + projectFile.getParentFile().mkdirs());

            FileWriter fw = new FileWriter(projectFile);
            fw.write(projectFile.getParentFile().getPath()+"/src/");
            fw.flush();
            fw.close();
            if(!new File(projectFile.getParentFile().getPath()+"/src/").mkdirs()){
                Log.e("FileSystem", "Error! Could not create folders!");
                return false;
            }else{
                return true;
            }
        }catch(IOException ioe){
            Log.e("FileSystem", "Could not create project: "+ioe.getMessage());
            ioe.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setProject(String projectName) {
        try {
            Scanner sc = new Scanner(getProjectFile(projectName));
            String path = sc.nextLine();
            baseDir = new File(path);
            currentDir = baseDir;
            sc.close();
            return true;
        }catch(IOException ioe){
            Log.e("FileSystem", "Error loading file: "+ioe.getMessage());
            ioe.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getExistingProjects(){
        List<String> projects = new ArrayList<>();
        File projectDir = new File(PROJECT_DIR);
        if(projectDir.listFiles() != null) {
            for (File f : projectDir.listFiles()) {
                if (f.isDirectory()) {
                    projects.add(f.getName());
                }
            }
        }

        return projects;
    }

    private File getProjectFile(String projectName){
        return new File(PROJECT_DIR+"/"+projectName+"/project.ini");
    }

    @Override
    public CodeFile getCurrentDir() {
        return (currentDir==null?null:new SimpleCodeFile(currentDir));
    }

    @Override
    public List<CodeFile> getFilesInCurrentDir() {
        List<CodeFile> files = new ArrayList<>();
        if(currentDir != null) {
            for (File f : currentDir.listFiles()) {
                files.add(new SimpleCodeFile(f));
            }
        }

        Collections.sort(files);
        return files;
    }

    @Override
    public String getCurrentProject() {
        return baseDir.getParentFile().getName();
    }

    @Override
    public CodeFile createFile(String name) {
        try {
            File f = new File(currentDir.getPath() + "/" + name);
            if(f.createNewFile()){
                return new SimpleCodeFile(f);
            }else{
                return null;
            }
        }catch(IOException ioe){
            Log.e("FileSystem", "Could not create file: "+ioe.getMessage());
            return null;
        }
    }

    @Override
    public CodeFile createDir(String name) {
        File f = new File(currentDir.getPath() + "/" + name);
        if(f.mkdir()){
            return new SimpleCodeFile(f);
        }else{
            return null;
        }
    }

    @Override
    public boolean stepUpOneLevel() {
        if(canStepUpOneLevel()) {
            currentDir = currentDir.getParentFile();
            return true;
        }
        return false;
    }

    @Override
    public boolean stepIntoDir(CodeFile dir) {
        if(currentDir != null) {
            currentDir = new File(currentDir.getPath() + "/" + dir.getName() + "/");
            return true;
        }
        return false;
    }

    @Override
    public boolean canStepUpOneLevel(){
        return baseDir != null && currentDir != null && !baseDir.equals(currentDir);
    }

    @Override
    public void saveFile(CodeFile f, String contents){
        if(f != null){
            f.saveContents(context, contents);
        }
    }
}
