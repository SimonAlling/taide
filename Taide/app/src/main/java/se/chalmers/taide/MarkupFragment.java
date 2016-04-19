package se.chalmers.taide;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MarkupFragment extends Fragment {

    int startPos = 0, endPos = 0;
    float dX0 = 0, dX1 = 0;
    boolean marked = false, pointer0Left = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markup, container, false);

            view.setOnTouchListener(new View.OnTouchListener() {


                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    EditText text = (EditText) getActivity().findViewById(R.id.editText);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            startPos = text.getSelectionStart();
                            dX0 = event.getX();
                            marked = false;
                            break;

                        case MotionEvent.ACTION_POINTER_DOWN:
                            dX0 = event.getX(0);
                            dX1 = event.getX(1);

                            if (dX0 - dX1 < 0) {
                                pointer0Left = true;
                            }
                            if(!marked)
                                endPos = startPos;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (event.getPointerCount() > 1) {
                                float x0 = event.getX(0);
                                float x1 = event.getX(1);
                                int maxLength = text.getText().length();
                                if (pointer0Left) {
                                    int newStartPos = getNewHandlePos(startPos, x0, dX0, maxLength);
                                    int newEndPos = getNewHandlePos(endPos, x1, dX1, maxLength);
                                    text.setSelection(newStartPos, newEndPos);
                                    if (newStartPos != startPos) {
                                        startPos = newStartPos;
                                        dX0 = x0;
                                    }
                                    if (newEndPos != endPos) {
                                        endPos = newEndPos;
                                        dX1 = x1;
                                    }
                                } else {
                                    int newStartPos = getNewHandlePos(startPos, x1, dX1, maxLength);
                                    int newEndPos = getNewHandlePos(endPos, x0, dX0, maxLength);
                                    text.setSelection(newStartPos, newEndPos);
                                    if (newStartPos != startPos) {
                                        startPos = newStartPos;
                                        dX1 = x1;
                                    }
                                    if (newEndPos != endPos) {
                                        endPos = newEndPos;
                                        dX0 = x0;
                                    }
                                }
                                if(startPos != endPos)
                                    marked = true;

                            } else {
                                if(!marked) {
                                    float x = event.getX();
                                    int maxLength = text.getText().length();
                                    int newPos = getNewHandlePos(startPos, x, dX0, maxLength);
                                    text.setSelection(newPos);
                                    if (newPos != startPos) {
                                        startPos = newPos;
                                        dX0 = x;
                                    }
                                }else {
                                    float x0 = event.getX(0);
                                    int maxLength = text.getText().length();
                                    if (pointer0Left) {
                                        int newStartPos = getNewHandlePos(startPos, x0, dX0, maxLength);
                                        text.setSelection(newStartPos, endPos);
                                        if (newStartPos != startPos) {
                                            startPos = newStartPos;
                                            dX0 = x0;
                                        }
                                    } else {
                                        int newEndPos = getNewHandlePos(endPos, x0, dX0, maxLength);
                                        text.setSelection(startPos, newEndPos);
                                        if (newEndPos != endPos) {
                                            endPos = newEndPos;
                                            dX0 = x0;
                                        }
                                    }
                                    if(startPos != endPos)
                                        marked = true;
                                }
                            }
                            return false;

                        case MotionEvent.ACTION_POINTER_UP:
                            if(marked)
                                text.setSelection(startPos, endPos);
                            return true;

                        case MotionEvent.ACTION_UP:
                            if(marked)
                                text.setSelection(startPos, endPos);
                            return true;

                        default:
                            return false;
                    }
                    return true;
                }

                public int getNewHandlePos(int pointer, float x, float dX, int maxLength) {
                    Point size = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                    int width = size.x;
                    if (Math.abs(x - dX) >= width / 75) {
                        if (x - dX < 0 && pointer > 0) {
                            return (pointer - 1);
                        } else if (x - dX > 0 && pointer < maxLength) {
                            return (pointer + 1);
                        }
                    }
                    return pointer;
                }
            });
        return view;
    }
}
