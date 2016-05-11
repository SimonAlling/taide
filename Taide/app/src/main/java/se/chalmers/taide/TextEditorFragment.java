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

import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.util.Clipboard;
import se.chalmers.taide.util.ViewUtil;

public class TextEditorFragment extends Fragment {

    private RadialActionMenuLayout leftActionMenu;
    private RadialActionMenuLayout rightActionMenu;

    private AutoFillPopupWindow autoFillWindow;
    private TextSource editorAsTextSource;
    private EditText codeEditor;
    private EditorModel model;

    private HashMap<Integer, Boolean> currentEnabledData;

    private Action[] getActionsRight(){
        return new Action[]{
                new Action.Insert(getActivity(), codeEditor, "(", "()"),
                new Action.Insert(getActivity(), codeEditor, "[", "[]"),
                new Action.Insert(getActivity(), codeEditor, "{", "{}")
        };
    }

    private Action[] getActionsLeft(){
        return new Action[]{
            new Action.ToggleTouchPad(getActivity()),
            new Action.Copy(getActivity(), codeEditor),
            new Action.Paste(getActivity(), codeEditor)
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_text_editor, container, false);

        // Retrieve the code editor text field
        codeEditor = (EditText) view.findViewById(R.id.editText);

        // Bind code editor to the model.
        initModel();

        //Bind action menus
        leftActionMenu = (RadialActionMenuLayout) view.findViewById(R.id.actionMenuLayoutLeft);
        leftActionMenu.setButtons(getActionsLeft());
        rightActionMenu = (RadialActionMenuLayout) view.findViewById(R.id.actionMenuLayoutRight);
        rightActionMenu.setButtons(getActionsRight());

        return view;
    }

    /**
     * Replaces the specified action buttons with buttons for the specified actions.
     * @param alignment The alignment of the buttons to replace (LEFT, RIGHT, or something else)
     */
    public void setActionButtons(RadialActionMenuLayout.Alignment alignment, Action[] actions) {
        switch (alignment) {
            case LEFT:
                leftActionMenu.setButtons(actions);
                break;
            case RIGHT:
                rightActionMenu.setButtons(actions);
                break;
            default:
                Log.w("warning", "Nothing is specified to happen when replacing action buttons with "+alignment+" alignment.");
        }
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
                case R.id.action_copy:  new Action.Copy(a, codeEditor).perform();break;
                case R.id.action_cut:   new Action.Cut(a, codeEditor).perform();break;
                case R.id.action_paste: new Action.Paste(a, codeEditor).perform();break;
                case R.id.action_save:
                    model.saveFile(null);
                    a.invalidateOptionsMenu();
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
        HashMap<Integer, Boolean> enabledData = calculateMenuItemEnabled();
        //Set all values
        for(Map.Entry<Integer, Boolean> entry : enabledData.entrySet()){
            menu.findItem(entry.getKey()).setEnabled(entry.getValue());
        }
        this.currentEnabledData = enabledData;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        //Detach autofill visual functionality
        if(autoFillWindow != null){
            autoFillWindow.detach();
        }
    }

    private HashMap<Integer, Boolean> calculateMenuItemEnabled(){
        HashMap<Integer, Boolean> data = new HashMap<>();
        data.put(R.id.action_paste, Clipboard.hasPasteContent(getActivity()));
        data.put(R.id.action_save, model.hasChangedCurrentFile());
        data.put(R.id.action_undo, model.peekUndo() != null);
        data.put(R.id.action_redo, model.peekRedo() != null);
        return data;
    }

    private void initModel(){
        if(model == null) {
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

            //Fix updates of menu
            editorAsTextSource.addListener(new TextSource.TextSourceListener() {
                @Override
                public boolean onTextChanged(String s, int start, int before, int count) {
                    if(currentEnabledData != null){
                        HashMap<Integer, Boolean> enabledData = calculateMenuItemEnabled();
                        if(!enabledData.equals(currentEnabledData)){
                            if(getActivity() != null) {
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                    }
                    return false;
                }

                @Override
                public int getPriority() {
                    return 0;
                }
            }, true);

            //Add new auto fill window
            autoFillWindow = new AutoFillPopupWindow(getActivity(), model, codeEditor);
        }
    }

}
