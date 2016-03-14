package se.chalmers.taide.model.filesystem;

import android.content.Context;

/**
 * Created by Matz on 2016-03-11.
 */
public interface CodeFile extends Comparable<CodeFile>{

    String getName();
    boolean isDirectory();

    String getContents();
    boolean saveContents(Context context, String contents);
    boolean remove();
    boolean rename(String newName);

}
