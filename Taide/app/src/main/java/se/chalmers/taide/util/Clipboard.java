package se.chalmers.taide.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

/**
 * Created by Matz on 2016-02-15.
 */
public class Clipboard {

    public static void copyToClipboard(Context context, EditText input){
        ClipboardManager manager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
        int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
        String text = input.getText().subSequence(start, end).toString();
        manager.setPrimaryClip(ClipData.newPlainText("Taide code", text));
    }

    public static void cutToClipboard(Context context, EditText input){
        copyToClipboard(context, input);
        input.getText().replace(Math.min(input.getSelectionStart(), input.getSelectionEnd()), Math.max(input.getSelectionStart(), input.getSelectionEnd()), "");
    }

    public static void pasteFromClipboard(Context context, EditText input){
        ClipboardManager manager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(manager.hasPrimaryClip() && manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
            ClipData clip = manager.getPrimaryClip();
            StringBuilder text = new StringBuilder();
            for(int i = 0; i<clip.getItemCount(); i++){
                text.append(clip.getItemAt(i).getText());
                if(i<clip.getItemCount()-1){
                    text.append("\n");
                }
            }

            int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
            int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
            input.getText().replace(start, end, text.toString());
        }
    }

    public static boolean hasPasteContent(Context context){
        ClipboardManager manager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        return manager.hasPrimaryClip() && manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
    }
}
