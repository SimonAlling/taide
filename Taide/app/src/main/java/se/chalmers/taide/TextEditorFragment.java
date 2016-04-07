package se.chalmers.taide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.Clipboard;
import se.chalmers.taide.util.TabUtil;

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
        // Init sample code
        final String sampleCode = "public class Main{\n\n"+ TabUtil.getTabs(1)+"public static void main(String[] args){\n"+TabUtil.getTabs(2)+"System.out.println(\"Hello world!\");\n"+TabUtil.getTabs(1)+"}\n\n}";
        codeEditor.setText(sampleCode);
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() <= sampleCode.length()+1 && getActivity() != null){
                    getActivity().invalidateOptionsMenu();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bind code editor to the model. Use Java as language
        model = ModelFactory.createEditorModel(ModelFactory.editTextToTextSource(codeEditor), LanguageFactory.JAVA);
        Log.d("MainActivity", "Started model with language: " + model.getLanguage().getName());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            Log.w("TextEditorFragment", "Could not perform action since activity was not attached");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setEnabled(true);
        menu.findItem(R.id.action_paste).setEnabled(Clipboard.hasPasteContent(getActivity()));
        menu.findItem(R.id.action_undo).setEnabled(model.peekUndo()!=null);
        menu.findItem(R.id.action_redo).setEnabled(model.peekRedo() != null);
    }
}
