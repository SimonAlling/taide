package se.chalmers.taide.model.filesystem;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import se.chalmers.taide.model.ProjectType;

/**
 * Created by Matz on 2016-03-11.
 */
public class SimpleFileSystem implements FileSystem{

    private Context context;

    private List<Project> projects;
    private Project currentProject;
    private File baseDir;
    private File currentDir;

    protected SimpleFileSystem(Context context){
        this.context = context;
        this.projects = new LinkedList<>();
        Environment.PROJECT_DIR = context.getFilesDir().getAbsolutePath();
        File projectDataFile = new File(Environment.PROJECT_DIR+"/"+Environment.PROJECTS_DATA_FILE);
        if(!projectDataFile.exists()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(projectDataFile));
                bw.write("");
                bw.close();
            }catch(IOException ioe){
                Log.w("FileSystem", "Could not create Projects Data File");
            }
        }else{
            try {
                Scanner sc = new Scanner(projectDataFile);
                while (sc.hasNextLine()) {
                    String[] data = sc.nextLine().split(" ");
                    if (data.length == 2) {
                        String name = data[0];
                        String type = data[1];
                        try{
                            projects.add(FileSystemFactory.getProject(name, ProjectType.valueOf(type)));
                        } catch(IllegalArgumentException ia){
                            Log.w("FileSystem", "Trying to load a project of unknown type: "+type);
                        }
                    }
                }
                sc.close();
            }catch(IOException ioe){
                Log.w("FileSystem", "Could not load projects");
            }
        }
    }

    @Override
    public boolean newProject(String projectName, ProjectType type, OnProjectLoadListener listener){
        //Use only last part as projectname.
        String concreteName = FileSystemFactory.getConcreteName(projectName, type);
        if(getExistingProjects().contains(concreteName)){
            return setProject(projectName, type, listener);
        }

        Project p = FileSystemFactory.getProject(projectName, type);
        boolean success = p.setupProject();
        if(success){
            projects.add(p);
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Environment.PROJECT_DIR+"/"+Environment.PROJECTS_DATA_FILE), true));
                bw.write(p.getName()+" "+type.name()+"\n");
                bw.close();
            }catch(IOException ioe){
                Log.w("FileSystem", "Could not add project to list of projects: "+ioe.getMessage());
            }
        }

        return success && setProject(projectName, type, listener);
    }

    @Override
    public boolean setProject(String projectName, ProjectType type, OnProjectLoadListener listener) {
        Project p = findProject(projectName);
        if(p != null) {
            baseDir = p.getBaseFolder();
            currentDir = baseDir;
            currentProject = p;
            p.loadData(listener);
            return true;
        }else{
            //Project not found.
            return false;
        }
    }

    private Project findProject(String name){
        for(Project p : projects){
            if(p.getName().equals(name)){
                return p;
            }
        }

        return null;
    }

    @Override
    public List<String> getExistingProjects(){
        List<String> projectNames = new LinkedList<>();
        for(Project p : this.projects){
            projectNames.add(p.getName());
        }

        return projectNames;
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
        if(currentProject != null) {
            return currentProject.createFile(currentDir.getPath(), name);
        }else{
            return null;
        }
    }

    @Override
    public CodeFile createDir(String name) {
        if(currentProject != null) {
            return currentProject.createDir(currentDir.getPath(), name);
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
            f.saveContents(contents);
        }
    }
}
