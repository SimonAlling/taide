package se.chalmers.taide.model;

import android.content.res.Resources;
import android.text.Editable;
import android.text.SpannableString;

/**
 * Created by Matz on 2016-02-29.
 */
public interface TextSource {

    Editable getText();

    void setSpannable(SpannableString str);

    int getSelectionStart();
    int getSelectionEnd();
    void setSelection(int selection);

    Resources getResources();

    void addListener(TextSourceListener tsl);
    void addListener(TextSourceListener tsl, boolean allowEventChaining);
    void removeListener(TextSourceListener tsl);



    interface TextSourceListener {
        void onTextChanged(String s, int start, int before, int count);
    }
}
