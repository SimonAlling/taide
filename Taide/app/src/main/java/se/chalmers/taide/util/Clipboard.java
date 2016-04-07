package se.chalmers.taide.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

/**
 * Created by Matz on 2016-02-15.
 *
 * Simplifies the use of clipboard functionality. All copy/paste
 * functionality passes through Androids default Clipboard, meaning
 * that other software will have access to the copied data as well.
 */
public class Clipboard {

    /**
     * Copies the selected text to the clipboard
     * @param context The current application context
     * @param input The text field to be copied from
     */
    public static void copyToClipboard(Context context, EditText input) {
        if(context != null) {
            ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
            int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
            String text = input.getText().subSequence(start, end).toString();
            manager.setPrimaryClip(ClipData.newPlainText("Taide code", text));
        }
    }

    /**
     * Cuts the selected text from the clipboard
     * @param context The current application context
     * @param input The text field to be cut from
     */
    public static void cutToClipboard(Context context, EditText input) {
        if(context != null) {
            copyToClipboard(context, input);
            input.getText().replace(Math.min(input.getSelectionStart(), input.getSelectionEnd()), Math.max(input.getSelectionStart(), input.getSelectionEnd()), "");
        }
    }

    /**
     * Pastes the content of the Android clipboard into the given text field at the
     * current selection.
     * @param context The current application context
     * @param input The text field to paste into
     */
    public static void pasteFromClipboard(Context context, EditText input) {
        if(context != null) {
            ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager.hasPrimaryClip() && manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData clip = manager.getPrimaryClip();
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < clip.getItemCount(); i++) {
                    text.append(clip.getItemAt(i).getText());
                    if (i < clip.getItemCount() - 1) {
                        text.append("\n");
                    }
                }

                int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
                int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
                input.getText().replace(start, end, text.toString());
            }
        }
    }

    /**
     * Checks whether the clipboard has any available text data to paste.
     * @param context The current application context
     * @return <code>true</code> if the clipboard has available text data, <code>false</code> otherwise
     */
    public static boolean hasPasteContent(Context context) {
        if(context == null){
            return false;
        }

        ClipboardManager manager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        return manager.hasPrimaryClip() && manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
    }
}
