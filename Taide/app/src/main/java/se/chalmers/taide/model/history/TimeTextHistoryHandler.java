package se.chalmers.taide.model.history;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.chalmers.taide.model.TextSource;

/**
 * Created by Matz on 2016-02-17.
 */
public class TimeTextHistoryHandler extends AbstractTextHistoryHandler {

    private String currentInputContent = "";
    private Action currentTextAction = null;
    private int currentTextPos = -1;
    private String currentTextInput = "";

    private Timer timer;
    private static final int COUNTDOWN_TIME = 1000;
    private boolean hasPendingTask = false;

    protected TimeTextHistoryHandler(){
        super();
        timer = new Timer("TextHistoryHandler");
    }

    @Override
    public void registerInputField(TextSource input){
        super.registerInputField(input);
        currentInputContent = (input==null?"":input.getText().toString());
    }

    @Override
    public boolean undoAction(){
        if(currentTextAction != null){
            insertCurrentData();
        }

        return super.undoAction();
    }

    @Override
    public String peekUndoAction(){
        String peek = super.peekUndoAction();
        if(peek == null){
            if(currentTextAction != null){
                List<TextAction> a = new LinkedList<>();
                a.add(new TextAction(currentTextAction, currentTextInput, currentTextPos));
                peek = actionListToString(a);
            }
        }

        return peek;
    }

    private TimerTask getCountdownTask(){
        return new TimerTask() {
            @Override
            public void run() {
                insertCurrentData();
            }
        };
    }

    private void insertCurrentData(){
        insertAction(new TextAction(currentTextAction, currentTextInput, currentTextPos));
        currentTextAction = null;
        currentTextInput = null;
        currentTextPos = -1;
        hasPendingTask = false;
    }


    @Override
    public void onTextChanged(String s, int start, int before, int count) {
        if(s.equals(currentInputContent)){
            //Ignore change.
            return;
        }

        timer.cancel();
        timer.purge();
        timer = new Timer("TextHistoryHandler");
        if(!hasPendingTask) {
            if (Math.abs(before-count) != 1 || (s.length()>start+before && !currentInputContent.substring(start, start+before).equals(s.substring(start, start+before)))) {  //Text replaced. (not only letter written)
                if(before > 0 && count > 0){
                    insertAction(new TextAction(Action.REMOVE, currentInputContent.substring(start, start+before), start),
                                 new TextAction(Action.ADD, s.substring(start, start+count), start));
                }else if(before > 0){
                    insertAction(new TextAction(Action.REMOVE, currentInputContent.substring(start, start+before), start));
                }else{
                    insertAction(new TextAction(Action.ADD, s.substring(start, start+count), start));
                }
                hasPendingTask = false;
            } else {
                this.currentTextPos = start;
                this.currentTextAction = (before < count ? Action.ADD : Action.REMOVE);
                if (currentTextAction == Action.ADD) {
                    currentTextPos += before;
                    this.currentTextInput = s.substring(currentTextPos, start + count);
                } else {
                    currentTextPos += count;
                    this.currentTextInput = currentInputContent.substring(currentTextPos, start+before);
                }
                timer.schedule(getCountdownTask(), COUNTDOWN_TIME);
                hasPendingTask = true;
            }
        }else{
            if(Math.abs(before-count) != 1 || (s.length()>start+before && !currentInputContent.substring(start, start+before).equals(s.substring(start, start+before)))){
                if(currentTextAction == Action.ADD && count>before && (start == currentTextPos || start+before == currentTextPos+currentTextInput.length())){
                    if(start+before == currentTextPos+currentTextInput.length() && before>0){
                        if(currentTextInput.length()>=before){
                            currentTextInput = currentTextInput.substring(0, currentTextInput.length()-before)+s.substring(start, start+count);
                        }else{
                            insertAction(new TextAction(Action.REMOVE, currentInputContent.substring(start, start+before-currentTextInput.length()), start));
                            currentTextPos = start;
                            currentTextInput = s.substring(start, start+count);
                        }
                    }else{
                        currentTextInput += s.substring(start, start+count);
                    }
                    timer.schedule(getCountdownTask(), COUNTDOWN_TIME);
                    hasPendingTask = true;
                }else {
                    //Save old state
                    insertCurrentData();
                    //Handle it as no task were found
                    onTextChanged(s, start, before, count);
                }
            }else{
                if(currentTextAction == Action.ADD){
                    if(before>count || start+before != currentTextPos+currentTextInput.length()){
                        //Change happens somewhere completely elsewhere. (or add -> remove)
                        insertCurrentData();
                        onTextChanged(s, start, before, count);
                    }else{
                        currentTextInput += s.substring(start+before, start+count);
                        timer.schedule(getCountdownTask(), COUNTDOWN_TIME);
                        hasPendingTask = true;
                    }
                }else if(currentTextAction == Action.REMOVE){
                    if(before<count || currentTextPos != start+count+1){
                        //Change happens somewhere completely elsewhere. (or remove -> add)
                        insertCurrentData();
                        onTextChanged(s, start, before, count);
                    }else{
                        currentTextPos -= 1;
                        currentTextInput = currentInputContent.charAt(start+count)+currentTextInput;
                        timer.schedule(getCountdownTask(), COUNTDOWN_TIME);
                        hasPendingTask = true;
                    }
                }else{
                    throw new UnsupportedOperationException("Cannot change a state that is not ADD or REMOVE");
                }
            }
        }

        currentInputContent = s;
    }
}
