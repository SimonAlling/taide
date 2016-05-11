package se.chalmers.taide;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import se.chalmers.taide.util.Clipboard;
import se.chalmers.taide.util.ViewUtil;

/**
 * Created by alling on 2016-05-05.
 */
public interface Action {

    String getName();
    int getIcon();
    void perform();

    abstract class AbstractAction implements Action{
        private String name;

        public AbstractAction(String name){ this.name = name; }
        public String getName(){ return name; }
        public int getIcon(){ return -1; }
    }

    /* ---------------- COPY/PASTE ACTIONS ------------------- */
    class Copy extends AbstractAction{
        private Context context;
        private EditText input;
        public Copy(Context context, EditText input){
            super(context.getResources().getString(R.string.action_button_copy));
            this.context = context;
            this.input = input;
        }
        public void perform(){
            Clipboard.copyToClipboard(context, input);
        }
    }

    class Paste extends AbstractAction{
        private Context context;
        private EditText input;
        public Paste(Context context, EditText input){
            super(context.getResources().getString(R.string.action_button_paste));
            this.context = context;
            this.input = input;
        }
        public void perform(){
            Clipboard.pasteFromClipboard(context, input);
        }
    }

    class Cut extends AbstractAction{
        private Context context;
        private EditText input;
        public Cut(Context context, EditText input){
            super(context.getResources().getString(R.string.action_button_yank));
            this.context = context;
            this.input = input;
        }
        public void perform(){
            Clipboard.cutToClipboard(context, input);
        }
    }

    /* ------------------------ INSERT ----------------------- */
    class Insert extends AbstractAction{
        private Context context;
        private EditText input;
        private String insertString;
        public Insert(Context context, EditText input, String insertString){
            this(context, input, insertString, insertString);
        }
        public Insert(Context context, EditText input, String insertString, String label){
            super(label);
            this.context = context;
            this.input = input;
            this.insertString = insertString;
        }
        public void perform(){
            int start = Math.max(Math.min(input.getSelectionStart(), input.getSelectionEnd()), 0);
            int end = Math.max(Math.max(input.getSelectionStart(), input.getSelectionEnd()), 0);
            input.getText().replace(start, end, insertString, 0, insertString.length());
        }
    }

    /* -------------------------- TOUCHPAD --------------------------- */
    class ToggleTouchPad extends AbstractAction{
        private Activity activity;
        public ToggleTouchPad(Activity activity){
            super(activity.getResources().getString(R.string.action_button_toggle_touchpad));
            this.activity = activity;
        }
        public void perform(){
            final View touchpad = activity.findViewById(R.id.markup);
            final boolean wasVisisble = touchpad.getVisibility() == View.VISIBLE;
            touchpad.setVisibility(wasVisisble ? View.GONE : View.VISIBLE);
            if(wasVisisble){
                ViewUtil.showSoftKeyboard(activity, null);
            } else{
                ViewUtil.hideSoftKeyboardTemporary(activity);
            }
        }
    }
}
