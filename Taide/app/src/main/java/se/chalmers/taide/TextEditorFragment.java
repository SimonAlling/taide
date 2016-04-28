package se.chalmers.taide;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.util.Clipboard;

public class TextEditorFragment extends Fragment {

    private ListPopupWindow autofillBox;
    private final TextSource.TextSourceListener cursorAnchorUpdateListener = new TextSource.TextSourceListener() {
        @Override
        public void onTextChanged(String s, int start, int before, int count) {
            updateAutofillBox();
        }
    };

    private TextSource editorAsTextSource;
    private EditText codeEditor;
    private EditorModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_editor, container, false);

        // Retrieve the code editor text field
        codeEditor = (EditText)view.findViewById(R.id.editText);

        // Bind code editor to the model.
        initModel();

        //Bind action menus
        RadialActionMenuLayout leftMenu = (RadialActionMenuLayout)view.findViewById(R.id.actionMenuLayoutLeft);
        leftMenu.setButtonTexts(new String[]{"0", "1", "2"});
        leftMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                switch (index) {
                    case 0:
                        View v = getActivity().findViewById(R.id.markup);
                        v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });
        RadialActionMenuLayout rightMenu = (RadialActionMenuLayout)view.findViewById(R.id.actionMenuLayoutRight);
        rightMenu.setButtonTexts(new String[]{"[]", "()", "{}"});
        rightMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                switch (index) {
                    case 0:
                        insertStringToCodeEditor("[");
                        break;
                    case 1:
                        insertStringToCodeEditor("(");
                        break;
                    case 2:
                        insertStringToCodeEditor("{");
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        initModel();
        initAutofillBox();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.text_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Activity a = getActivity();
        if(a != null) {
            switch (id) {
                case R.id.action_settings:
                    break;
                case R.id.action_copy:
                    Clipboard.copyToClipboard(a, codeEditor);
                    break;
                case R.id.action_cut:
                    Clipboard.cutToClipboard(a, codeEditor);
                    break;
                case R.id.action_paste:
                    Clipboard.pasteFromClipboard(a, codeEditor);
                    break;
                case R.id.action_undo:
                    model.undo();
                    a.invalidateOptionsMenu();
                    break;
                case R.id.action_redo:
                    model.redo();
                    a.invalidateOptionsMenu();
                    break;
            }
        }else{
            Log.w("TextEditor", "Could not perform action since activity was not attached");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_paste).setEnabled(Clipboard.hasPasteContent(getActivity()));
        menu.findItem(R.id.action_undo).setEnabled(model.peekUndo() != null);
        menu.findItem(R.id.action_redo).setEnabled(model.peekRedo() != null);
    }

    private void initModel(){
        if(model == null) {
            model = ModelFactory.getCurrentEditorModel();
            if(editorAsTextSource != null){
                editorAsTextSource.removeListener(cursorAnchorUpdateListener);
            }
            editorAsTextSource = ModelFactory.editTextToTextSource(codeEditor);
            editorAsTextSource.addListener(cursorAnchorUpdateListener);
            if (model == null) {
                if(codeEditor != null) {
                    model = ModelFactory.createEditorModel(getActivity(), editorAsTextSource);
                    Log.d("TextEditor", "Started model for text editor.");
                }else{
                    Log.w("TextEditor", "WARNING: No functional model in use!");
                }
            }else{
                model.setTextSource(editorAsTextSource);
                Log.d("TextEditor", "Fetched existing model and setup editor.");
            }
        }
    }

    private void insertStringToCodeEditor(String insertString){
        int start = Math.max(Math.min(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        int end = Math.max(Math.max(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        codeEditor.getText().replace(start, end, insertString, 0, insertString.length());
    }


    /* Auto fill code */
    private void initAutofillBox(){
        autofillBox = new ListPopupWindow(getActivity());
        autofillBox.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        autofillBox.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
        autofillBox.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        autofillBox.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void updateAutofillBox(){
        if(codeEditor != null) {
            int pos = codeEditor.getSelectionStart();
            Layout layout = codeEditor.getLayout();
            int line = layout.getLineForOffset(pos);
            int baseline = layout.getLineBaseline(line);
            int ascent = layout.getLineAscent(line);
            float x = layout.getPrimaryHorizontal(pos);
            float y = baseline + ascent;
            View cursorAnchor = getView().findViewById(R.id.cursorAnchor);
            cursorAnchor.setX(x);
            cursorAnchor.setY(y);
            cursorAnchor.setVisibility(View.VISIBLE);

            updateAutofillWindowState(cursorAnchor);
        }
    }

    private void updateAutofillWindowState(View anchorView){
        List<String> values = getAutofillValues();
        if(values != null) {
            ListAdapter adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, values);
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

    private ArrayList<String> getAutofillValues(){
        String autofillReplacer = (model != null?model.getAutoFillReplacement():null);
        if(autofillReplacer != null){
            ArrayList<String> list = new ArrayList<>();
            list.add(autofillReplacer);
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
                mMeasureParent = new FrameLayout(getActivity());
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
