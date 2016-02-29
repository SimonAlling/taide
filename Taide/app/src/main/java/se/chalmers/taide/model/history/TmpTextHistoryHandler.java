package se.chalmers.taide.model.history;

import android.util.Log;

/**
 * Created by Matz on 2016-02-18.
 */
public class TmpTextHistoryHandler extends AbstractTextHistoryHandler{

    private String prevString = "";

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d("HistoryHandler", "RECV[start="+start+", before="+before+", count="+count+", s.length()="+s.length()+"]");
        if(start>0 && start+count<s.length()){
            Log.d("HistoryHandler", "\t'"+s.subSequence(start, start+count)+"'");
        }else{
            if(s.toString().equals(prevString)){
                Log.d("HistoryHandler", "\tSame stuff");
            }else {
                Log.d("HistoryHandler", "\tIndexes too random.");
            }
        }
        prevString = s.toString();
    }
}
