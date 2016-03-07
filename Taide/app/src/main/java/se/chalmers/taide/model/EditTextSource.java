package se.chalmers.taide.model;

import android.content.res.Resources;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matz on 2016-02-29.
 */
public class EditTextSource implements TextSource {

    private EditText input;
    private List<TextSourceListener> listeners;

    protected EditTextSource(EditText input) {
        this.input = input;
        this.input.addTextChangedListener(new TextWatcher() {
            private String currentInputContent = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.equals(currentInputContent)){
                    //Log.d("AbstractTextFilter", "No text has changed, ignoring.");
                    return;
                }else{
                    currentInputContent = s.toString();
                }

                String str = s.toString();
                for (TextSourceListener listener : listeners) {
                    listener.onTextChanged(str, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        this.listeners = new LinkedList<>();
    }

    @Override
    public Editable getText() {
        return input.getText();
    }

    @Override
    public void setSpannable(SpannableString str) {
        input.setText(str, TextView.BufferType.SPANNABLE);
    }

    @Override
    public int getSelectionStart() {
        return Math.min(input.getSelectionStart(), input.getSelectionEnd());
    }

    @Override
    public int getSelectionEnd() {
        return Math.max(input.getSelectionStart(), input.getSelectionEnd());
    }

    @Override
    public void setSelection(int selection) {
        input.setSelection(selection);
    }

    @Override
    public Resources getResources() {
        return input.getResources();
    }

    @Override
    public void addListener(TextSourceListener tsl) {
        if (tsl != null) {
            listeners.add(tsl);
        }
    }

    @Override
    public void removeListener(TextSourceListener tsl) {
        listeners.remove(tsl);
    }
}
