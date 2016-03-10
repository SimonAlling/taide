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
    private boolean applyingFilters = false;

    protected EditTextSource(EditText input) {
        this.input = input;
        this.input.addTextChangedListener(new TextWatcher() {
            private String currentInputContent = "";
            private String fullContent = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Make sure that anything actually has changed
                if(s.toString().equals(currentInputContent) || applyingFilters){
                    //Ignore change. Do not chain filter changes.
                    return;
                }else{
                    //Handle swap in/out of full document
                    if(fullContent != null){
                        if(start == 0 && before == 0 && count == fullContent.length()){
                            currentInputContent = fullContent;
                            fullContent = null;
                            return;
                        }else{
                            //Send previous call.
                            for (TextSourceListener listener : listeners) {
                                listener.onTextChanged("", 0, fullContent.length(), 0);
                            }
                        }
                        fullContent = null;
                    }
                    if(start == 0 && before == currentInputContent.length() && count == 0){
                        fullContent = currentInputContent;
                        currentInputContent = s.toString();
                        return;
                    }

                    currentInputContent = s.toString();
                }

                applyingFilters = true;
                String str = s.toString();
                for (TextSourceListener listener : listeners) {
                    listener.onTextChanged(str, start, before, count);
                }
                applyingFilters = false;
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
        int start = input.getSelectionStart(), end = input.getSelectionEnd();
        input.getText().clear();
        input.setText(str, TextView.BufferType.SPANNABLE);
        input.setSelection(start, end);
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
