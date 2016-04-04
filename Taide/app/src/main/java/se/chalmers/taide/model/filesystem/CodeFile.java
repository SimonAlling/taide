package se.chalmers.taide.model.filesystem;

/**
 * Created by Matz on 2016-03-11.
 */
public interface CodeFile extends Comparable<CodeFile>{

    String getName();
    String getUniqueName();
    boolean isDirectory();
    boolean isOpenable();

    String getContents();
    boolean saveContents(String contents);
    boolean remove();
    boolean rename(String newName);

}
