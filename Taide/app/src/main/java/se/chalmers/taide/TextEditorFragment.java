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

    private static final String ACTION_BUTTON_FALLBACK_LABEL = "?";

    // These control the action button configurations (in CLOCKWISE order):
    // Any number of actions can be specified here; the number of buttons will follow.
    // LEFT action button:
    private static final Action[] ACTIONS_LEFT = {
            Action.TOGGLE_TOUCHPAD,
            Action.COPY,
            Action.PASTE
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

    private RadialActionMenuLayout leftActionMenu;
    private RadialActionMenuLayout rightActionMenu;

    private AutoFillPopupWindow autoFillWindow;
    private TextSource editorAsTextSource;
    private EditText codeEditor;
    private EditorModel model;

    private HashMap<Integer, Boolean> currentEnabledData;

    /**
     * Returns the labels for the specified actions in the same order.
     * @param actions The actions whose labels are desired
     * @return A string array with labels for the specified actions
     */
    private String[] getActionButtonLabels(Action[] actions) {
        String[] labels = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            final Integer labelStringID = ACTION_BUTTON_LABELS.get(actions[i]);
            // If there's no label mapped to the action, we will use a fallback dummy label.
            // We could have used actions[i].toString(), but this is better since it makes it
            // obvious that we have failed to map a label properly:
            labels[i] = labelStringID == null ? ACTION_BUTTON_FALLBACK_LABEL : getString(labelStringID);
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
        actionHandler(action);
    }

    private void actionHandler(Action action) {
        switch (action) {
            case COPY:
                Clipboard.copyToClipboard(getActivity(), codeEditor);
                break;
            case YANK:
                Clipboard.cutToClipboard(getActivity(), codeEditor);
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
                Log.w("warning", "Nothing is specified to happen for action "+action+".");
                break;
        }
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
        leftActionMenu.setButtons(getActionButtonLabels(ACTIONS_LEFT));
        leftActionMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                // This will be called whenever the LEFT action menu is used:
                actionButtonHandler(RadialActionMenuLayout.Alignment.LEFT, index);
            }
        });
        rightActionMenu = (RadialActionMenuLayout) view.findViewById(R.id.actionMenuLayoutRight);
        rightActionMenu.setButtons(getActionButtonLabels(ACTIONS_RIGHT));
        rightActionMenu.setActionForAll(new RadialActionMenuLayout.OnActionButtonTriggeredListener() {
            @Override
            public void actionButtonTriggered(int index) {
                // This will be called whenever the RIGHT action menu is used:
                actionButtonHandler(RadialActionMenuLayout.Alignment.RIGHT, index);
            }
        });

        return view;
    }

    /**
     * Replaces the specified action buttons with buttons for the specified actions.
     * @param alignment The alignment of the buttons to replace (LEFT, RIGHT, or something else)
     * @param actions The actions to create and insert action buttons for
     */
    public void setActionButtons(RadialActionMenuLayout.Alignment alignment, Action[] actions) {
        switch (alignment) {
            case LEFT:
                leftActionMenu.setButtons(getActionButtonLabels(actions));
                break;
            case RIGHT:
                rightActionMenu.setButtons(getActionButtonLabels(actions));
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

    private void insertStringToCodeEditor(String insertString){
        int start = Math.max(Math.min(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        int end = Math.max(Math.max(codeEditor.getSelectionStart(), codeEditor.getSelectionEnd()), 0);
        codeEditor.getText().replace(start, end, insertString, 0, insertString.length());
    }

    private void toggleTouchpad() {
        final View touchpad = getActivity().findViewById(R.id.markup);
        final boolean wasVisisble = touchpad.getVisibility() == View.VISIBLE;
        touchpad.setVisibility(wasVisisble ? View.GONE : View.VISIBLE);
        if(wasVisisble){
            ViewUtil.showSoftKeyboard(getActivity(), null);
        } else{
            ViewUtil.hideSoftKeyboardTemporary(getActivity());
        }
    }

}
