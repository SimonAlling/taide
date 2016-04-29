package se.chalmers.taide.model.history;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-04-17.
 */
public class WordTextHistoryHandler extends AbstractTextHistoryHandler {

    private String currentInputContent = "";
    private AbstractTextHistoryHandler.Action currentTextAction = null;
    private int currentTextPos = -1;
    private String currentTextInput = "";

    @Override
    public void registerInputField(TextSource input) {
        super.registerInputField(input);
        currentInputContent = (input == null ? "" : input.getText().toString());
        currentTextPos = (input == null ? 0 : input.getSelectionStart());
    }

    @Override
    public boolean undoAction() {
        if (currentTextAction != null) {
            insertCurrentData();
        }
        return super.undoAction();
    }

    @Override
    public String peekUndoAction() {
        if (currentTextAction != null) {
            List<TextAction> actions = new LinkedList<>();
            actions.add(new TextAction(currentTextAction, currentTextInput, currentTextPos));
            return actionListToString(actions);
        } else {
            return super.peekUndoAction();
        }
    }

    private void insertCurrentData() {
        if (currentTextAction != null && currentTextInput.length() > 0) {
            insertAction(new AbstractTextHistoryHandler.TextAction(currentTextAction, currentTextInput, currentTextPos));
            currentTextAction = null;
            currentTextInput = "";
            currentTextPos = -1;
        }
    }

    private boolean hasMovedCursor(int start, int before, int count) {
        return currentTextAction == null ||
                (currentTextAction == Action.ADD && start-currentTextInput.length() != currentTextPos) ||
                (currentTextAction == Action.REMOVE && start+before != currentTextPos);
    }


    @Override
    public boolean onTextChanged(String s, int start, int before, int count) {
        if (s.equals(currentInputContent)) {
            //Ignore change.
            return false;
        }

        // Handle extreme case
        if (before == count) {
            if (before != 0 && count != 0) {
                onTextChanged(s, start, before, 0);
                onTextChanged(s, start, 0, count);
            }
            return false;
        }

        if (hasMovedCursor(start, before, count) || (currentTextAction == Action.ADD && before > count) || (currentTextAction == Action.REMOVE && count > before)) {
            insertCurrentData();
            currentTextPos = start;
            currentTextAction = (count > before ? Action.ADD : Action.REMOVE);
        }

        if (Math.abs(count - before) == 1) {
            if (before > count) {
                if (currentInputContent.charAt(start+before-1) == ' ') {
                    currentTextPos = start+1;
                    insertCurrentData();
                }
                currentTextPos = start;
                currentTextInput = currentInputContent.charAt(start+before-1)+currentTextInput;
            } else {
                currentTextInput += s.charAt(start+count-1);
                if (s.charAt(start+count-1) == ' ') {
                    insertCurrentData();
                }
            }
        } else {
            if (before > count) {
                String changedContent = currentInputContent.substring(start+count, start+before);
                int end = changedContent.length(), spaceIndex;
                do {
                    spaceIndex = end;
                    while (spaceIndex > 0 && changedContent.charAt(spaceIndex - 1) == ' ') {
                        // Handle multiple spaces in a row
                        spaceIndex--;
                    }
                    spaceIndex = changedContent.lastIndexOf(" ", Math.max(0, spaceIndex-1));

                    currentTextInput = changedContent.substring(spaceIndex+1, end) + currentTextInput;
                    if (spaceIndex >= 0) {
                        end = spaceIndex+1;
                        currentTextPos = start+end;
                        insertCurrentData();
                        currentTextPos = start+end;
                        currentTextAction = Action.REMOVE;
                    }
                } while (spaceIndex > 0);
                currentTextPos = start;
            } else {
                String changedContent = s.substring(start+before, start+count);
                int currentStart = 0, spaceIndex;
                do {
                    spaceIndex = changedContent.indexOf(" ", currentStart);
                    if (spaceIndex >= 0) {
                        while (spaceIndex + 1 < changedContent.length() && changedContent.charAt(spaceIndex + 1) == ' ') {
                            // Handle multiple spaces
                            spaceIndex++;
                        }
                    }

                    currentTextInput += changedContent.substring(currentStart, (spaceIndex < 0 ? changedContent.length() : spaceIndex+1));
                    if (spaceIndex >= 0) {
                        insertCurrentData();
                        currentTextAction = Action.ADD;
                        currentStart = spaceIndex+1;
                        currentTextPos = start+currentStart;
                    }
                } while (spaceIndex >= 0);
            }
        }

        currentInputContent = s;
        return false;
    }
}
