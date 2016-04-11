package se.chalmers.taide.model.filesystem;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Matz on 2016-03-11.
 */
public class SimpleCodeFile implements CodeFile {

    private static List<String> deniedFileFormats = Arrays.asList(new String[]{"jpg", "jpeg", "png", "gif", "psd", "zip", "gz", "tar"});

    private File source;

    protected SimpleCodeFile(File source){
        this.source = source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public String getUniqueName() {
        return source.getPath()+"/"+source.getName();
    }

    @Override
    public String getFileFormat(){
        String name = getName();
        if(name.contains(".")){
            return name.substring(name.lastIndexOf(".")+1);
        }else{
            return "";
        }
    }

    @Override
    public boolean isDirectory() {
        return source.isDirectory();
    }

    @Override
    public boolean isOpenable(){
        String fileFormat = source.getName().substring(source.getName().lastIndexOf(".")+1);
        return !deniedFileFormats.contains(fileFormat);
    }

    @Override
    public String getContents() {
        try {
            Scanner sc = new Scanner(source);
            StringBuffer b = new StringBuffer();
            while (sc.hasNextLine()) {
                b.append(sc.nextLine()).append("\n");
            }
            sc.close();

            return b.toString();
        }catch(IOException ioe){
            Log.d("CodeFile", "Could not read file: " + ioe.getMessage());
            return "";
        }
    }

    @Override
    public boolean saveContents(String contents) {
        if(!isDirectory()) {
            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(source));
                out.write(contents.getBytes());
                out.flush();
                out.close();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        }else{
            if(!source.exists()){
                return source.mkdir();
            }else{
                return true;
            }
        }
    }

    @Override
    public boolean rename(String newName) {
        File newSource = new File(source.getParentFile().getPath()+"/"+newName);
        if(source.renameTo(newSource)) {
            source = newSource;
            return true;
        }

        return false;
    }

    @Override
    public boolean remove() {
        if(source.isDirectory()){
            boolean success = true;
            //Delete recursively down.
            for(File f : source.listFiles()){
                success = success && new SimpleCodeFile(f).remove();
            }
            if(success){
                if(source.delete()){
                    source = null;
                    return true;
                }
            }
        }else {
            if (source.delete()) {
                source = null;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj){
        return (obj instanceof SimpleCodeFile && ((SimpleCodeFile)obj).source.equals(this.source));
    }

    @Override
    public int compareTo(CodeFile another) {
        if(this.equals(another)){
            return 0;
        }

        if(isDirectory() != another.isDirectory()){
            return (isDirectory()?-1:1);
        }else{
            return this.getName().compareTo(another.getName());
        }
    }
}
