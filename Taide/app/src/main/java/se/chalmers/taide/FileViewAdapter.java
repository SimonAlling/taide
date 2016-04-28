package se.chalmers.taide;

import android.content.Context;
import android.util.TypedValue;
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
        super(context, android.R.layout.simple_list_item_1, prepareArray(values));
        this.context = context;
        this.canStepUpOneLevel = canStepUpOneLevel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context;
        View view;
        if (convertView == null) {
            context = (parent != null ? parent.getContext() : this.context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.icon_text_item_view_files, parent, false);
        } else {
            context = convertView.getContext();
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(R.id.item_name);
        ImageView icon = (ImageView) view.findViewById(R.id.item_icon);
        if (position == 0) {
            textView.setText(R.string.upOneLevel);
            textView.setEnabled(canStepUpOneLevel);
            icon.setImageResource(getReferenceFromStyle(context, R.attr.iconUpOneFileLevel, R.drawable.ic_up_one_file_level_white));
        } else {
            CodeFile f = getItem(position);
            textView.setText(f.getName());
            if (f.isDirectory()) {
                icon.setImageResource(getReferenceFromStyle(context, R.attr.iconFolder, R.drawable.ic_folder_white));
            } else {
                textView.setEnabled(f.isOpenable());
                icon.setImageResource(getReferenceFromStyle(context, R.attr.iconFile, R.drawable.ic_file_white));
            }
        }

        int textColor = getReferenceFromStyle(context, textView.isEnabled() ? android.R.attr.textColorPrimary : android.R.attr.textColorSecondary, android.R.color.black);
        textView.setTextColor(context.getResources().getColor(textColor));

        return view;
    }

    private int getReferenceFromStyle(Context context, int attribute, int def) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attribute, typedValue, false);
        return typedValue.data == 0 ? def : typedValue.data;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 0;
    }

    private static CodeFile[] prepareArray(CodeFile[] array) {
        CodeFile[] arr = new CodeFile[array.length+1];
        arr[0] = null;
        System.arraycopy(array, 0, arr, 1, array.length);
        return arr;
    }
}
