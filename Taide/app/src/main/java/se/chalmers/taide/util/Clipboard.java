package se.chalmers.taide.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

import se.chalmers.taide.R;

/**
 * Created by Matz on 2016-02-15.
 *
 * Simplifies the use of clipboard functionality. All copy/paste
 * functionality passes through Androids default Clipboard, meaning
 * that other software will have access to the copied data as well.
 */
public class Clipboard {

    public static final String DEFAULT_LABEL = "Taide code";

    /**
     * Copies the selected text to the clipboard
     * @param context The current application context
     * @param input The text field to be copied from
     */
    public static void copyToClipboard(Context context, EditText input) {
        if (context != null) {
            final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            final int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
            final int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
            final String selectedText = input.getText().subSequence(start, end).toString();
            // No need to replace clipboard content with the empty string, so let's first check if
            // there was any selected text:
            if (selectedText.length() > 0) {
                final String label = context.getString(R.string.clipboard_label, DEFAULT_LABEL);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(label, selectedText));
            }
        }
    }

    /**
     * Cuts the selected text from the clipboard
     * @param context The current application context
     * @param input The text field to be cut from
     */
    public static void cutToClipboard(Context context, EditText input) {
        if (context != null) {
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
        if (context != null) {
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
        if (context == null){
            return false;
        }

        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return manager.hasPrimaryClip() && manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
    }
}
