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
import se.chalmers.taide.model.filesystem.dropbox.DropboxFactory;

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

                            //Load dropbox dependency if needed
                            if(ProjectType.valueOf(type) == ProjectType.DROPBOX && !DropboxFactory.isAuthenticated()){
                                DropboxFactory.initDropboxIntegration(context);
                            }
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
            Log.d("FileSystem", "Does not create project, it already exists.");
            return setProject(concreteName, listener);
        }

        Project p = FileSystemFactory.getProject(projectName, type);
        Log.d("FileSystem", "Project created of type "+p.getClass().getName());
        boolean success = p.setupProject();
        if(success){
            projects.add(p);
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Environment.PROJECT_DIR+"/"+Environment.PROJECTS_DATA_FILE), true));
                bw.write(projectName+" "+type.name()+"\n");
                bw.close();
            }catch(IOException ioe){
                Log.w("FileSystem", "Could not add project to list of projects: "+ioe.getMessage());
                success = false;
            }
        }

        return success && setProject(concreteName, listener);
    }

    @Override
    public boolean setProject(String projectName, OnProjectLoadListener listener) {
        Project p = findProject(projectName);
        if(p != null) {
            Log.d("FileSystem", "Setting project "+p.getName()+" of type "+p.getClass().getName());
            baseDir = p.getBaseFolder();
            currentDir = baseDir;
            currentProject = p;
            p.loadData(listener);
            return true;
        }else{
            //Project not found.
            Log.e("FileSystem", "Could not set project as active since it was not found.");
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
        return (currentDir==null?null:currentProject.getCodeFile(currentDir));
    }

    @Override
    public CodeFile findFileByName(String name){
        File f = new File(currentProject.getBaseFolder().getPath()+"/"+name);
        return (f.exists()?currentProject.getCodeFile(f):null);
    }

    @Override
    public List<CodeFile> getFilesInCurrentDir() {
        List<CodeFile> files = new ArrayList<>();
        if(currentDir != null) {
            for (File f : currentDir.listFiles()) {
                files.add(currentProject.getCodeFile(f));
            }
        }

        Collections.sort(files);
        return files;
    }

    @Override
    public String getCurrentProject() {
        if(baseDir == null) {
            return null;
        }else {
            return baseDir.getParentFile().getName();
        }
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
        Log.d("FileSystem", "Creating dir: '"+name+"' using project of type "+(currentProject==null?"null":currentProject.getClass().getName()));
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
