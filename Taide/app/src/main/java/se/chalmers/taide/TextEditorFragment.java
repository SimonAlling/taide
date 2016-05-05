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

import java.util.Map;
import java.util.EnumMap;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.TextSource;
import se.chalmers.taide.util.Clipboard;

public class TextEditorFragment extends Fragment {

    // These control the action button configurations (in CLOCKWISE order):
    // LEFT action button:
    private static final Action[] ACTIONS_LEFT = {
            Action.TOGGLE_TOUCHPAD,
            Action.PASTE,
            Action.COPY
    };
    // RIGHT action button:
    private static final Action[] ACTIONS_RIGHT = {
            Action.INSERT_BRACKET,
            Action.INSERT_HARD_BRACKET,
            Action.INSERT_CURLY_BRACKET
    };

    // Each action should have a string reference mapped to itself:
    private static final Map<Action, Integer> ACTION_BUTTON_LABELS = new EnumMap<>(Action.class);
    static {
        ACTION_BUTTON_LABELS.put(Action.COPY, R.string.action_button_copy);
        ACTION_BUTTON_LABELS.put(Action.YANK, R.string.action_button_yank);
        ACTION_BUTTON_LABELS.put(Action.PASTE, R.string.action_button_paste);
        ACTION_BUTTON_LABELS.put(Action.TOGGLE_TOUCHPAD, R.string.action_button_toggle_touchpad);
        ACTION_BUTTON_LABELS.put(Action.INSERT_BRACKET, R.string.action_button_insert_bracket);
        ACTION_BUTTON_LABELS.put(Action.INSERT_HARD_BRACKET, R.string.action_button_insert_hard_bracket);
        ACTION_BUTTON_LABELS.put(Action.INSERT_CURLY_BRACKET, R.string.action_button_insert_curly_bracket);
    }

    /**
     * Returns the labels for the specified actions in the same order.
     * @param actions The actions whose labels are desired
     * @return A string array with labels for the specified actions
     */
    private String[] getActionButtonLabels(Action[] actions) throws NullPointerException {
        String[] labels = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            final Integer labelStringID = ACTION_BUTTON_LABELS.get(actions[i]);
            if (labelStringID == null) {
                throw new NullPointerException("Could not get label string for "+actions[i]+" because it has no string ID mapped to it.");
            }
            labels[i] = getString(labelStringID);
        }
        return labels;
    }

    /**
     * Handles action button presses in a clever and robust way.
     * @param actionButtonAlignment The alignment (e.g. LEFT) of the used mother action button
     * @param index The index of the child button (in clockwise order)
     */
    private void actionButtonHandler(RadialActionMenuLayout.Alignment actionButtonAlignment, int index) {
        // Right now we know the alignment (LEFT, RIGHT, or possibly something else) of the mother
        // action button that was used, and we know the index (in clockwise order) of the child
        // button that was pressed.
        // From there we can find out which (if any) action should be performed.
        Action action;
        switch (actionButtonAlignment) {
            case LEFT:
                action = ACTIONS_LEFT[index];
                break;
            case RIGHT:
                action = ACTIONS_RIGHT[index];
                break;
            default:
                Log.w("warning", "There is no code for handling action buttons with "+actionButtonAlignment+" alignment. Doing nothing.");
                return;
        }
        // The above switch statement instead of a ternary expression may seem a little odd at
        // first, but careful thought has been put behind it: If another alignment, e.g. CENTER, is
        // introduced later, a ternary expression would cause a very nasty logical bug. With the
        // current structure, if no case clause for CENTER alignment is added, a press on a button
        // with CENTER alignment will simply not trigger any action at all (except a useful log
        // entry), which is exactly what we want.
        switch (action) {
            case COPY:
                Clipboard.copyToClipboard(getActivity(), codeEditor);
                break;
            case PASTE:
                Clipboard.pasteFromClipboard(getActivity(), codeEditor);
                break;
            case TOGGLE_TOUCHPAD:
                toggleTouchpad();
                break;
            case INSERT_BRACKET:
                insertStringToCodeEditor("(");
                break;
            case INSERT_HARD_BRACKET:
                insertStringToCodeEditor("[");
                break;
            case INSERT_CURLY_BRACKET:
                insertStringToCodeEditor("{");
                break;
            default:
                Log.w("warning", "Nothing is specified to happen for action "+action+". It was triggered by an action button with "+actionButtonAlignment+" alignment and index "+index+".");
        }
    }


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
        final View view = inflater.inflate(R.layout.fragment_text_editor, container, false);

        // Retrieve the code editor text field
        codeEditor = (EditText) view.findViewById(R.id.editText);

        // Bind code editor to the model.
        initModel();

        //Bind action menus
        final RadialActionMenuLayout leftMenu = (RadialActionMenuLayout) view.findViewById(R.id.actionMenuLayoutLeft);
        leftMenu.setButtonTexts(getActionButtonLabels(ACTIONS_LEFT));
        leftMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                // This will be called whenever the LEFT action menu is used:
                actionButtonHandler(RadialActionMenuLayout.Alignment.LEFT, index);
            }
        });
        final RadialActionMenuLayout rightMenu = (RadialActionMenuLayout) view.findViewById(R.id.actionMenuLayoutRight);
        rightMenu.setButtonTexts(getActionButtonLabels(ACTIONS_RIGHT));
        rightMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                // This will be called whenever the RIGHT action menu is used:
                actionButtonHandler(RadialActionMenuLayout.Alignment.RIGHT, index);
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

    private void toggleTouchpad() {
        final View touchpad = getActivity().findViewById(R.id.markup);
        touchpad.setVisibility(touchpad.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

}
