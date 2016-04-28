package se.chalmers.taide;

import android.app.Activity;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.model.AutoFill;
import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-04-28.
 */
public class AutoFillPopupWindow implements TextSource.TextSourceListener{

    private ListPopupWindow autofillBox;
    private String currentDisabledAutofillWord;

    private Activity currentActivity;
    private EditorModel model;
    private EditText codeEditor;

    public AutoFillPopupWindow(Activity activity, EditorModel model, EditText codeEditor){
        this.currentActivity = activity;
        this.model = model;
        this.codeEditor = codeEditor;
        if(model != null && model.getTextSource() != null){
            model.getTextSource().addListener(this);
        }
        initAutoFillBox();
    }

    public void detach(){
        if(model != null && model.getTextSource() != null){
            model.getTextSource().removeListener(this);
        }
    }

    @Override
    public void onTextChanged(String s, int start, int before, int count) {
        updateAutoFillBox();
    }
    @Override
    public int getPriority(){ return 100; }


    private void initAutoFillBox(){
        autofillBox = new ListPopupWindow(currentActivity);
        autofillBox.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        autofillBox.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
        autofillBox.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        autofillBox.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        autofillBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutoFill currentAutofill = (model==null?null:model.getAutoFillReplacement());
                if(currentAutofill != null){
                    currentDisabledAutofillWord = currentAutofill.getTrigger();
                    model.setAutofillWordEnabled(currentDisabledAutofillWord, false);
                    autofillBox.dismiss();
                }
            }
        });
    }

    private void updateAutoFillBox(){
        if(codeEditor != null) {
            int pos = codeEditor.getSelectionStart();
            Layout layout = codeEditor.getLayout();
            int line = layout.getLineForOffset(pos);
            int baseline = layout.getLineBaseline(line);
            int ascent = layout.getLineAscent(line);
            float x = layout.getPrimaryHorizontal(pos);
            float y = baseline + ascent;
            View cursorAnchor = currentActivity.findViewById(R.id.cursorAnchor);
            cursorAnchor.setX(x);
            cursorAnchor.setY(y);
            cursorAnchor.setVisibility(View.VISIBLE);

            //Update UI
            updateAutoFillWindowState(cursorAnchor);

            //Reset disabled word
            if(currentDisabledAutofillWord != null){
                model.setAutofillWordEnabled(currentDisabledAutofillWord, true);
                currentDisabledAutofillWord = null;
            }
        }
    }

    private void updateAutoFillWindowState(View anchorView){
        List<String> values = getAutoFillValues();
        if(values != null) {
            ListAdapter adapter = new ArrayAdapter<>(currentActivity.getApplicationContext(), android.R.layout.simple_list_item_1, values);
            autofillBox.setAdapter(adapter);
            autofillBox.setContentWidth(measureContentWidth(adapter));
            if(!autofillBox.isShowing()){
                autofillBox.show();
            }
        }else if(autofillBox.isShowing()) {
            autofillBox.dismiss();
        }

        //Set anchor
        autofillBox.setAnchorView(anchorView);
    }

    private ArrayList<String> getAutoFillValues(){
        AutoFill autofill = (model != null?model.getAutoFillReplacement():null);
        if(autofill != null && model != null && model.getTextSource() != null){
            ArrayList<String> list = new ArrayList<>();
            String source = model.getTextSource().getText().toString();
            int index = model.getTextSource().getSelectionStart();
            list.add(autofill.getPrefix(source, index) + autofill.getSuffix(source, index));
            return list;
        }else{
            return null;
        }
    }

    private int measureContentWidth(ListAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final ListAdapter adapter = listAdapter;
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(currentActivity);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }

}