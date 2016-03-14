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
    private List<TextSourceListener> listenersAllowChains;
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
                if(s.toString().equals(currentInputContent)){
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

                String str = s.toString();
                //Apply for all listeners that allow chaining
                for(TextSourceListener listener : listenersAllowChains){
                    listener.onTextChanged(str, start, before, count);
                }

                //Don't apply to normal unless it's an immediate event (from the edittext itself)
                if(!applyingFilters) {
                    applyingFilters = true;
                    for (TextSourceListener listener : listeners) {
                        listener.onTextChanged(str, start, before, count);
                    }
                    applyingFilters = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        this.listeners = new LinkedList<>();
        this.listenersAllowChains = new LinkedList<>();
    }

    @Override
    public Editable getText() {
        return input.getText();
    }

    @Override
    public void setText(String text){
        input.setSelection(0);
        input.setText(text);
    }

    @Override
    public void setSpannable(SpannableString str) {
        int start = Math.min(input.getSelectionStart(), input.getSelectionEnd());
        int end = Math.max(input.getSelectionStart(), input.getSelectionEnd());
        //input.getText().clear();
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
        addListener(tsl, false);
    }

    @Override
     public void addListener(TextSourceListener tsl, boolean allowEventChaining) {
        if (tsl != null) {
            if(allowEventChaining){
                listenersAllowChains.add(tsl);
            }else {
                listeners.add(tsl);
            }
        }
    }

    @Override
    public void removeListener(TextSourceListener tsl) {
        listeners.remove(tsl);
        listenersAllowChains.remove(tsl);
    }
}
