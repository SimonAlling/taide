package se.chalmers.taide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.zip.Inflater;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.util.Clipboard;

public class TextEditorFragment extends Fragment {

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
                    case 0: View v = getActivity().findViewById(R.id.markup);

                            v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                            break;
                    default: break;
                }
            }
        });
        RadialActionMenuLayout rightMenu = (RadialActionMenuLayout)view.findViewById(R.id.actionMenuLayoutRight);
        rightMenu.setButtonTexts(new String[]{"[]", "()", "{}"});
        rightMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                switch (index) {
                    case 0: insertStringToCodeEditor("[");break;
                    case 1: insertStringToCodeEditor("(");break;
                    case 2: insertStringToCodeEditor("{");break;
                }
            }
        });

        return view;
    }

    private void updateCursorAnchor(){
        int pos = codeEditor.getSelectionStart();
        Layout layout = codeEditor.getLayout();
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        float x = layout.getPrimaryHorizontal(pos);
        float y = baseline + ascent;
        Log.d("debug", "Coordinates: (" + x + ", " + y + ")");
        View cursorAnchor = getView().findViewById(R.id.cursorAnchor);
        cursorAnchor.setX(x);
        cursorAnchor.setY(y);
        cursorAnchor.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        initModel();
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
        menu.findItem(R.id.action_undo).setEnabled(model.peekUndo()!=null);
        menu.findItem(R.id.action_redo).setEnabled(model.peekRedo() != null);
    }

    private void initModel(){
        if(model == null) {
            model = ModelFactory.getCurrentEditorModel();
            TextSource textSource = ModelFactory.editTextToTextSource(codeEditor);
            initAutofill(textSource);
            if (model == null) {
                if(codeEditor != null) {
                    model = ModelFactory.createEditorModel(getActivity(), textSource);
                    Log.d("TextEditor", "Started model for text editor.");
                }else{
                    Log.w("TextEditor", "WARNING: No functional model in use!");
                }
            }else{
                model.setTextSource(textSource);
                Log.d("TextEditor", "Fetched existing model and setup editor.");
            }
        }
    }

    private void initAutofill(TextSource textSource){
        textSource.addListener(new TextSource.TextSourceListener() {
            @Override
            public void onTextChanged(String s, int start, int before, int count) {
                updateCursorAnchor();
            }
        });
    }

    private void insertStringToCodeEditor(String insertString){
        int start = Math.max(Math.min(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        int end = Math.max(Math.max(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        codeEditor.getText().replace(start, end, insertString, 0, insertString.length());
    }
}
