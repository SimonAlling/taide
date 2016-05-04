package se.chalmers.taide;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.util.Clipboard;

public class TextEditorFragment extends Fragment {

    private AutoFillPopupWindow autoFillWindow;
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
                Activity a = getActivity();
                switch (index) {
                    case 0:
                        View v = getActivity().findViewById(R.id.markup);
                        v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        break;
                    case 1:
                        Clipboard.copyToClipboard(a, codeEditor);
                        break;
                    case 2:
                        Clipboard.pasteFromClipboard(a, codeEditor);
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
            //Detach autofill visual functionality
            if(autoFillWindow != null){
                autoFillWindow.detach();
            }

            //Update references
            model = ModelFactory.getCurrentEditorModel();
            editorAsTextSource = ModelFactory.editTextToTextSource(codeEditor);
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

            //Add new auto fill window
            autoFillWindow = new AutoFillPopupWindow(getActivity(), model, codeEditor);
        }
    }

    private void insertStringToCodeEditor(String insertString){
        int start = Math.max(Math.min(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        int end = Math.max(Math.max(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        codeEditor.getText().replace(start, end, insertString, 0, insertString.length());
    }


}
