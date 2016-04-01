package se.chalmers.taide.model.filesystem;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Matz on 2016-03-22.
 */
public class SimpleProject implements Project{

    private String name;
    private File baseFolder;

    public SimpleProject(String name){
        this.name = name;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public void loadData(FileSystem.OnProjectLoadListener listener) {
        baseFolder = getBaseFolder();
        if(listener != null){
            listener.projectLoaded(true);
        }
    }

    @Override
    public CodeFile createFile(String folder, String name) {
        File f = new File(baseFolder.getPath() + "/" +folder + (folder.length()>0?"/":"") + name);
        try {
            if (f.createNewFile()) {
                return new SimpleCodeFile(f);
            }
        }catch(IOException ioe){
            Log.e("Project", "Could not create file: "+ioe.getMessage());
        }

        return null;
    }

    @Override
    public CodeFile createDir(String folder, String name) {
        File f = new File(baseFolder.getPath()+"/"+folder + (folder.length()>0?"/":"") + name);
        if(f.mkdir()){
            return new SimpleCodeFile(f);
        }else{
            return null;
        }
    }

    @Override
    public CodeFile getCodeFile(File f) {
        return new SimpleCodeFile(f);
    }

    @Override
    public boolean setupProject() {
        try {
            File projectFile = getProjectFile();
            Log.d("FileSystem", "Creating folders: " + projectFile.getParentFile().mkdirs());

            FileWriter fw = new FileWriter(projectFile);
            fw.write(projectFile.getParentFile().getPath() + "/src/");
            fw.flush();
            fw.close();
            File sourceFile = new File(projectFile.getParentFile().getPath()+"/src/");
            if(!sourceFile.exists() && !sourceFile.mkdirs()){
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
    public File getBaseFolder() {
        try {
            Scanner sc = new Scanner(getProjectFile());
            String folder = sc.nextLine();
            sc.close();
            return new File(folder);
        }catch(IOException ioe){
            Log.w("Project", "Invalid contents in project file!");
            return null;
        }
    }

    protected File getProjectFile(){
        return new File(Environment.PROJECT_DIR+"/"+name+"/project.ini");
    }
}
