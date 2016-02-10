package se.chalmers.taide.model;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import se.chalmers.taide.model.languages.Language;

/**
 * Created by Matz on 2016-02-10.
 */
public abstract class AbstractTextFilter implements TextFilter, TextWatcher{

    private Language language;
    private EditText editText;
    private String[] triggerTexts;

    protected AbstractTextFilter(String... triggerTexts){
        this.triggerTexts = triggerTexts;
    }

    protected abstract void applyFilterEffect();

    protected EditText getTextView(){
        return editText;
    }

    protected Language getLanguage(){
        return language;
    }


    public void attach(EditText editText){
        if(this.editText != null){
            throw new IllegalStateException("Cannot attach to new EditText, an attachment is already in use");
        }

        this.editText = editText;
        this.editText.addTextChangedListener(this);
    }

    public void detach(){
        if(this.editText != null) {
            this.editText.removeTextChangedListener(this);
            this.editText = null;
        }
    }

    public void setLanguage(Language lang){
        this.language = lang;
    }

    protected void setTriggerText(String... triggerTexts){
        this.triggerTexts = triggerTexts;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Update filter if correct input received
        String input = s.toString().substring(start, start + count);
        for (String triggerText : triggerTexts){
            if (input.equals(triggerText)) {
                applyFilterEffect();
                break;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}

}
