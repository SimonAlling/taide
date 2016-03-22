package se.chalmers.taide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import se.chalmers.taide.model.filesystem.CodeFile;

/**
 * Created by Matz on 2016-03-11.
 */
public class FileViewAdapter extends ArrayAdapter<CodeFile>{
    private final Context context;
    private boolean canStepUpOneLevel;

    public FileViewAdapter(Context context, CodeFile[] values, boolean canStepUpOneLevel) {
        super(context, -1, prepareArray(values));
        this.context = context;
        this.canStepUpOneLevel = canStepUpOneLevel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fileview, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.fileview_name);
        ImageView icon = (ImageView)rowView.findViewById(R.id.fileview_icon);
        if(position == 0){
            textView.setText(R.string.upOneLevel);
            textView.setEnabled(canStepUpOneLevel);
            if(!textView.isEnabled()) {
                textView.setTextColor(context.getResources().getColor(R.color.disabledText));
            }
            icon.setImageResource(R.mipmap.ic_uponelevel);
        }else{
            CodeFile f = getItem(position);
            textView.setText(f.getName());
            if(f.isDirectory()){
                icon.setImageResource(R.mipmap.ic_folder);
            }else{
                icon.setImageResource(R.mipmap.ic_file);
            }
        }

        return rowView;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 1;
        }else{
            return 0;
        }
    }

    private static CodeFile[] prepareArray(CodeFile[] array){
        CodeFile[] arr = new CodeFile[array.length+1];
        arr[0] = null;
        System.arraycopy(array, 0, arr, 1, array.length);
        return arr;
    }
}

