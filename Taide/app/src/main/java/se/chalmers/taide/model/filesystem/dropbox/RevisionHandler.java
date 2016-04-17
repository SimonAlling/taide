package se.chalmers.taide.model.filesystem.dropbox;

import android.util.Log;

import com.dropbox.client2.DropboxAPI;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import se.chalmers.taide.model.filesystem.Project;

/**
 * Created by Matz on 2016-04-04.
 */
public class RevisionHandler {

    private static final String REVISION_FILE_NAME = "revision_state.log";
    private static final String REVISION_FILE_SEPARATOR = " splitter ";

    private static Map<String, RevisionHandler> registeredHandlers = new HashMap<>();

    private Project project;
    private String baseFolder;
    private FileTree revisionState;
    private FileTree newRevisionState = FileTree.rootNode();

    public RevisionHandler(Project project, String baseFolder){
        this.project = project;
        this.baseFolder = (baseFolder.startsWith("/")?"":"/")+baseFolder+(baseFolder.endsWith("/")?"":"/");
    }

    public static RevisionHandler getInstance(String projectName){
        if(!registeredHandlers.containsKey(projectName)){
            Log.e("RevisionHandler", "Requesting an uninitiated project.");
            return null;
        }else{
            return registeredHandlers.get(projectName);
        }
    }

    public static RevisionHandler registerHandler(Project project, String baseFolder){
        RevisionHandler handler = new RevisionHandler(project, baseFolder);
        registeredHandlers.put(project.getName(), handler);
        return handler;
    }

    public void loadRevisionState(){
        Log.d("RevisionHandler", "Loading revision state file...");
        if(revisionState == null){
            revisionState = FileTree.rootNode();
            File file = new File(project.getBaseFolder().getParentFile().getPath()+"/"+REVISION_FILE_NAME);
            if(file.exists()){
                try{
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine()){
                        String[] lineData = sc.nextLine().split(REVISION_FILE_SEPARATOR);
                        if(lineData.length == 2){
                            FileTree t = new FileTree(lineData[0], lineData[1]);
                            String parent = t.filename.endsWith("/")?t.filename.substring(0, t.filename.lastIndexOf("/", t.filename.lastIndexOf("/") - 2) + 1):t.filename.substring(0, t.filename.lastIndexOf("/")+1);
                            FileTree.findChild(revisionState, parent, baseFolder).addChild(t);
                        }
                    }
                    sc.close();
                }catch(Exception e){
                    Log.e("RevisionHandler", "Invalid revision file, could not load! ("+e.getMessage()+")");
                }
            }
        }
    }

    public List<String> getRemovedFiles(){
        List<String> files = new LinkedList<>();
        retrieveRemovedFiles(files, revisionState, newRevisionState);
        return files;
    }

    private void retrieveRemovedFiles(List<String> files, FileTree oldTree, FileTree newTree){
        for(FileTree child : oldTree.children){
            FileTree newChild = null;
            for(FileTree aspirant : newTree.children){
                if(aspirant.filename.equalsIgnoreCase(child.filename)){
                    newChild = aspirant;
                    break;
                }
            }

            if(newChild == null){
                addToRemovalList(files, child);
            }else{
                retrieveRemovedFiles(files, child, newChild);
            }
        }
    }

    private void addToRemovalList(List<String> files, FileTree tree){
        files.add(tree.filename);
        for(FileTree t : tree.children){
            addToRemovalList(files, t);
        }
    }

    public void saveRevisionData(){
        Log.d("RevisionHandler", "Saving revision state file...");
        StringBuffer b = new StringBuffer();
        retrieveRevisionString(b, newRevisionState);

        try {
            File file = new File(project.getBaseFolder().getParentFile().getPath() + "/" + REVISION_FILE_NAME);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false));
            out.write(b.toString().getBytes());
            out.flush();
            out.close();
        }catch(IOException ioe){
            Log.e("RevisionHandler", "Could not save revision file");
        }

        revisionState = newRevisionState;
        newRevisionState = FileTree.rootNode();
    }

    private void retrieveRevisionString(StringBuffer buf, FileTree tree){
        if(!tree.filename.equals(FileTree.rootNode().filename)) {
            buf.append(tree.filename).append(REVISION_FILE_SEPARATOR).append(tree.rev).append("\n");
        }

        for(FileTree t : tree.children){
            retrieveRevisionString(buf, t);
        }
    }

    public boolean shouldSyncEntry(DropboxAPI.Entry entry){
        String path = entry.parentPath()+entry.fileName()+(entry.isDir?"/":"");
        FileTree t = FileTree.findChild(revisionState, path, baseFolder);
        boolean shouldSync = (t == revisionState || !t.rev.equals(entry.rev));
        FileTree parent = FileTree.findChild(newRevisionState, entry.parentPath(), baseFolder);
        parent.addChild(new FileTree(entry.parentPath()+entry.fileName()+(entry.isDir?"/":""), entry.rev));

        return shouldSync;
    }

    public void updateSingleEntry(String file, String rev){
        newRevisionState = revisionState;
        FileTree parent = FileTree.findChild(newRevisionState, file.substring(0, file.lastIndexOf("/")+1), baseFolder);
        boolean hasBeenUpdated = false;
        for(FileTree child : parent.children){
            if(child.filename.equalsIgnoreCase(file)){
                child.rev = rev;
                hasBeenUpdated = true;
            }
        }

        if(!hasBeenUpdated){
            parent.addChild(new FileTree(file, rev));
        }

        saveRevisionData();
    }



    private static class FileTree{
        private List<FileTree> children;
        private String filename;
        private String rev;

        public FileTree(String filename, String rev){
            this.children = new LinkedList<>();
            this.filename = filename;
            this.rev = rev;
        }

        public void addChild(FileTree tree){
            if(tree != null){
                children.add(tree);
            }
        }
        public void removeChild(FileTree tree){
            children.remove(tree);
        }


        public static FileTree rootNode(){
            return new FileTree("[ROOT]", "[ROOT]");
        }

        public static void printFileTree(FileTree tree, String prefix){
            Log.d("FileTree", prefix+"[file="+tree.filename+", rev="+tree.rev+"]");
            prefix += "\t";
            for(FileTree t : tree.children){
                printFileTree(t, prefix);
            }
        }

        public static FileTree findChild(FileTree root, String path, String staticPath){
            //Make sure input is of same standard
            path = path.toLowerCase();
            staticPath = staticPath.toLowerCase();

            //Figure out stuff concerning prefixes
            boolean isDir = path.endsWith("/");
            String currentPath = "";
            if(path.startsWith(staticPath)){
                path = path.substring(staticPath.length());
                if(staticPath.endsWith("/")){
                    staticPath = staticPath.substring(0, staticPath.length()-1);
                }
            }

            //Setup stuff depending on findings about prefixes
            String[] paths;
            if(path.length()==0){
                paths = new String[]{staticPath};
            }else{
                paths = ("/"+path).split("/");
                paths[0] = staticPath;
            }

            //Search loop
            FileTree currentTree = root;
            boolean foundChild = false;
            for(int i = 0; i<paths.length; i++){
                currentPath += paths[i]+((isDir||i<paths.length-1)&&!paths[i].endsWith("/")?"/":"");
                for(FileTree t : currentTree.children){
                    if(t.filename.equalsIgnoreCase(currentPath)){
                        currentTree = t;
                        foundChild = true;
                        break;
                    }
                }
                if(!foundChild){
                    return root;
                }
                foundChild = false;     //Reset
            }

            return currentTree;
        }
    }
}
