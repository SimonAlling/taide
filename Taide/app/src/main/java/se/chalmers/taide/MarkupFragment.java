package se.chalmers.taide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MarkupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markup, container, false);

            view.setOnTouchListener(new View.OnTouchListener() {
                int pX = 0;
                float dX = 0;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    EditText text = (EditText) getActivity().findViewById(R.id.editText);
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            pX = text.getSelectionStart();
                            dX = event.getX();
                            Log.d("Action_Down", "Should do nothing really");
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float x = event.getX();
                            if(Math.round(x/10) > Math.round(dX/10)) {
                                pX = pX + 1;
                                text.setSelection(pX);
                            }else if((Math.round(x/10) < Math.round(dX/10)) && pX > 0) {
                                pX = pX - 1;
                                text.setSelection(pX);
                            }
                            dX = x;
                            return true;
                        case MotionEvent.ACTION_UP:
                            return false;
                        default: return false;
                    }
                }
            });
        return view;
    }
}
