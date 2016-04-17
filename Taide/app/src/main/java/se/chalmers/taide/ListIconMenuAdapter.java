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
 * Created by Matz on 2016-04-17.
 */
public class ListIconMenuAdapter extends ArrayAdapter<String>{

    private Context context;
    private String[] values;
    private int[] iconRefs;
    private boolean refsIsDynamic;

    public ListIconMenuAdapter(Context context, String[] values, int[] iconRefs, boolean refsIsDynamic){
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.iconRefs = iconRefs;
        this.refsIsDynamic = refsIsDynamic;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Find correct context
        Context context = (convertView!=null?convertView.getContext():(parent!=null?parent.getContext():this.context));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.icon_text_item_view, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.item_name);
        textView.setText(values[position]);
        ImageView icon = (ImageView)rowView.findViewById(R.id.item_icon);
        int iconRef = (refsIsDynamic?getReferenceFromStyle(context, iconRefs[position], 0):iconRefs[position]);
        icon.setImageResource(iconRef);

        int textColor = getReferenceFromStyle(context, textView.isEnabled() ? android.R.attr.textColorPrimary : android.R.attr.textColorSecondary, android.R.color.black);
        textView.setTextColor(context.getResources().getColor(textColor));

        return rowView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private int getReferenceFromStyle(Context context, int attribute, int def){
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(attribute, tv, false);
        return tv.data==0?def:tv.data;
    }
}
