package se.chalmers.taide.model.history;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.taide.R;
import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public abstract class AbstractTextHistoryHandler implements TextHistoryHandler, TextSource.TextSourceListener{

    private TextSource inputField;
    private List<List<TextAction>> actions;
    private int currentIndex = -1;

    private int invertCounter = 0;

    protected AbstractTextHistoryHandler(){
        actions = new ArrayList<>();
    }

    /**
     * Register text view that changes should be recorded in
     * @param input The text view to observe
     */
    @Override
    public void registerInputField(TextSource input) {
        if(input != this.inputField) {
            //Remove last text source
            if (this.inputField != null) {
                //Reset old field
                this.inputField.removeListener(this);
            }

            //Enable new text source
            this.inputField = input;
            if(this.inputField != null){
                this.inputField.addListener(this, true);
            }
        }
    }

    /**
     * Retrieves the currently bound text source to this handler.
     * @return The currently bound text source.
     */
    @Override
    public TextSource getTextSource(){
        return inputField;
    }

    public String getCurrentState(){
        StringBuffer b = new StringBuffer();
        int index = 1;
        for(List<TextAction> actionList : actions){
            b.append(index++).append(". [").append(actionListToString(actionList)).append("]").append("\n");
        }
        return b.toString();
    }

    /**
     * Retrieves a textual description of what action will be performed if
     * undoTextAction() is called.
     * @return A textual description of the current undo action
     */
    @Override
    public String peekUndoAction() {
        if(currentIndex >= 0 && currentIndex<actions.size()) {
            List<TextAction> actionList = actions.get(currentIndex);
            return actionListToString(actionList);
        }

        return null;
    }

    /**
     * Undo the last change that was performed on the text view
     * @return If the action was successful
     */
    @Override
    public boolean undoAction() {
        if(currentIndex >= 0 && currentIndex<actions.size()){
            boolean result = true;
            List<TextAction> actionList = actions.get(currentIndex);
            for(TextAction action : actionList){
                if(!invertAction(action)){
                    result = false;
                }
            }

            if(result) {
                currentIndex--;
            }
            return result;
        }

        return false;
    }

    /**
     * Retrieves a textual description of what action will be performed if
     * redoTextAction() is called
     * @return A textual description of the current redo action
     */
    @Override
    public String peekRedoAction() {
        //Is there anything to undo?
        if(currentIndex>=-1 && currentIndex < actions.size()-1){
            List<TextAction> actionList = actions.get(currentIndex+1);
            return actionListToString(actionList);
        }

        return null;
    }

    /**
     * Redo the last change that was performed with
     * @return If the action was successful
     */
    @Override
    public boolean redoAction() {
        if(currentIndex>=-1 && currentIndex < actions.size()-1){
            boolean result = true;
            List<TextAction> actionList = this.actions.get(currentIndex+1);
            for(TextAction action : actionList){
                TextAction a = new TextAction(action.getAction().invert(), action.getText(), action.getPosition());
                if(!invertAction(a)){
                    result = false;
                }
            }

            if(result) {
                currentIndex++;
            }
            return result;
        }

        return false;
    }

    private boolean invertAction(TextAction action){
        if(inputField != null) {
            //Fix invert settings
            invertCounter++;
            //Performing real inversion
            switch (action.getAction()) {
                case ADD:       try{
                                    inputField.getText().replace(action.getPosition(), action.getPosition()+action.getText().length(), "");
                                    inputField.setSelection(action.getPosition());
                                    return true;
                                }catch(IndexOutOfBoundsException iob){
                                    Log.e("HistoryHandler", "Error found: "+iob.getMessage());
                                    for(List<TextAction> a : actions){
                                        Log.d("HistoryHandler", "\t"+actionListToString(a));
                                    }
                                    return false;
                                }
                case REMOVE:    try{
                                    inputField.getText().replace(action.getPosition(), action.getPosition(), action.getText());
                                    inputField.setSelection(action.getPosition()+action.getText().length());
                                    return true;
                                }catch(IndexOutOfBoundsException iob) {
                                    Log.e("HistoryHandler", "Error found: " + iob.getMessage());
                                    return false;
                                }
                }
        }

        return false;
    }

    private String getActionName(TextAction action){
        if(inputField != null && inputField.getResources() != null){
            int resourceId = 0;
            switch(action.getAction()){
                case ADD:       resourceId = R.string.history_add_text;break;
                case REMOVE:    resourceId = R.string.history_remove_text;break;
                default:        throw new UnsupportedOperationException("Found an invalid action: "+action.getAction());
            }
            return inputField.getResources().getString(resourceId);
        }

        return action.getAction().name();
    }

    protected String actionListToString(List<TextAction> actionList){
        StringBuffer b = new StringBuffer();
        boolean first = true;
        for(TextAction action : actionList){
            b.append(getActionName(action)).append(" '").append(action.getText()).append("'");
            if(!first){
                b.append(", ");
            }
            first = false;
        }

        return b.toString();
    }

    protected void insertAction(TextAction... action){
        if(invertCounter==0) {
            //Clear old branches forward
            while (actions.size() > currentIndex + 1) {
                actions.remove(currentIndex + 1);
            }

            //Add new item
            //Log.d("HistoryHandler", "Adding actions: " + Arrays.toString(action));
            List<TextAction> actionList = new ArrayList<>();
            for (TextAction a : action) {
                if(a.getAction() != null) {
                    actionList.add(a);
                }
            }
            this.actions.add(actionList);
            currentIndex = actions.size()-1;
        }else{
            invertCounter = Math.max(0, invertCounter-action.length);
        }
    }

    @Override
    public abstract void onTextChanged(String s, int start, int before, int count);

    protected enum Action{
        ADD, REMOVE;

        public Action invert(){
            switch(this){
                case ADD:       return REMOVE;
                case REMOVE:    return ADD;
                default:        throw new UnsupportedOperationException("The given action has no inversion");
            }
        }
    }

    protected class TextAction{
        private Action action;
        private String text;
        private int position;

        protected TextAction(Action action, String text, int position){
            this.action = action;
            this.text = text;
            this.position = position;
        }

        public Action getAction(){
            return action;
        }

        public String getText(){
            return text;
        }

        private int getPosition(){
            return position;
        }

        @Override
        public String toString(){
            return "TextAction[action="+(action==null?"null":action.name())+", text='"+text+"', pos="+position+"]";
        }
    }
}
