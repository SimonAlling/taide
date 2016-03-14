package se.chalmers.taide.model.filesystem;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Created by Matz on 2016-03-11.
 */
public class SimpleCodeFile implements CodeFile {

    private File source;

    protected SimpleCodeFile(File source){
        this.source = source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public boolean isDirectory() {
        return source.isDirectory();
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
    public boolean saveContents(Context context, String contents) {
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(source));
            out.write(contents.getBytes());
            out.close();
            return true;
        }catch(IOException ioe){
            return false;
        }
    }

    @Override
    public boolean rename(String newName) {
        return source.renameTo(new File(newName));
    }

    @Override
    public boolean remove() {
        return source.delete();
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
