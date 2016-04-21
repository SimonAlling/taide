package se.chalmers.taide;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

public class MarkupFragment extends Fragment {

    int startPos = 0, endPos = 0, fragmentHeight = 0;
    float dX0 = 0, dX1 = 0, dY0 = 0, dY1 = 0;
    boolean marked = false, pointer0Left = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markup, container, false);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (fragmentHeight == 0) {
                    fragmentHeight = getActivity().findViewById(R.id.markup).getLayoutParams().height;
                }
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText text = (EditText) getActivity().findViewById(R.id.editText);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        startPos = text.getSelectionStart();
                        dX0 = event.getX();
                        dY0 = event.getY();
                        marked = false;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        dX0 = event.getX(0);
                        dX1 = event.getX(1);

                        if (dX0 - dX1 < 0) {
                            pointer0Left = true;
                        } else {
                            pointer0Left = false;
                        }
                        if (!marked)
                            endPos = startPos;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() > 1) {
                            float x0 = event.getX(0);
                            float x1 = event.getX(1);
                            int maxLength = text.getText().length();
                            if (pointer0Left) {
                                int newStartPos = getNewHandlePosX(startPos, x0, dX0, maxLength);
                                int newEndPos = getNewHandlePosX(endPos, x1, dX1, maxLength);
                                if (newStartPos <= newEndPos) {
                                    text.setSelection(newStartPos, newEndPos);

                                    if (newStartPos != startPos) {
                                        startPos = newStartPos;
                                        dX0 = x0;
                                    }
                                    if (newEndPos != endPos) {
                                        endPos = newEndPos;
                                        dX1 = x1;
                                    }
                                }
                                float y0 = event.getY(0);
                                float y1 = event.getY(1);
                                int newStartPosY = getNewHandlePosY(text.getText().toString(), startPos, y0, dY0);
                                int newEndPosY = getNewHandlePosY(text.getText().toString(), endPos, y1, dY1);
                                if (newStartPosY <= newEndPosY) {
                                    text.setSelection(newStartPosY, newEndPosY);

                                    if (newStartPosY != startPos) {
                                        startPos = newStartPosY;
                                        dY0 = y0;
                                    }
                                    if (newEndPosY != endPos) {
                                        endPos = newEndPosY;
                                        dY1 = y1;
                                    }
                                }
                            } else {
                                int newStartPos = getNewHandlePosX(startPos, x1, dX1, maxLength);
                                int newEndPos = getNewHandlePosX(endPos, x0, dX0, maxLength);
                                if (newStartPos <= newEndPos) {
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
                                float y0 = event.getY(0);
                                float y1 = event.getY(1);
                                int newStartPosY = getNewHandlePosY(text.getText().toString(), startPos, y1, dY1);
                                int newEndPosY = getNewHandlePosY(text.getText().toString(), endPos, y0, dY0);
                                if (newStartPosY <= newEndPosY) {
                                    text.setSelection(newStartPosY, newEndPosY);

                                    if (newStartPosY != startPos) {
                                        startPos = newStartPosY;
                                        dY1 = y1;
                                    }
                                    if (newEndPosY != endPos) {
                                        endPos = newEndPosY;
                                        dY0 = y0;
                                    }
                                }
                            }
                            if (startPos != endPos)
                                marked = true;

                        } else {
                            if (!marked) {
                                float x = event.getX();
                                int maxLength = text.getText().length();
                                int newPos = getNewHandlePosX(startPos, x, dX0, maxLength);
                                text.setSelection(newPos);
                                if (newPos != startPos) {
                                    startPos = newPos;
                                    dX0 = x;
                                }
                                float y = event.getY();
                                int newPosY = getNewHandlePosY(text.getText().toString(), startPos, y, dY0);
                                text.setSelection(newPosY);
                                if (newPosY != startPos) {
                                    startPos = newPosY;
                                    dY0 = y;
                                }
                            } else {
                                float x0 = event.getX(0);
                                int maxLength = text.getText().length();
                                if (pointer0Left) {
                                    int newStartPos = getNewHandlePosX(startPos, x0, dX0, maxLength);
                                    if (newStartPos <= endPos) {
                                        text.setSelection(newStartPos, endPos);

                                        if (newStartPos != startPos) {
                                            startPos = newStartPos;
                                            dX0 = x0;
                                        }
                                    }
                                } else {
                                    int newEndPos = getNewHandlePosX(endPos, x0, dX0, maxLength);
                                    if (startPos <= newEndPos) {
                                        text.setSelection(startPos, newEndPos);

                                        if (newEndPos != endPos) {
                                            endPos = newEndPos;
                                            dX0 = x0;
                                        }
                                    }
                                }
                                if (startPos != endPos)
                                    marked = true;
                            }
                        }
                        return false;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (marked)
                            text.setSelection(startPos, endPos);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (marked)
                            text.setSelection(startPos, endPos);
                        return true;

                    default:
                        return false;
                }
                return true;
            }

            public int getNewHandlePosX(int pointer, float x, float dX, int maxLength) {
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

            public int getNewHandlePosY(String text, int pointer, float y, float dY) {
                if (Math.abs(y - dY) > fragmentHeight / 10) {
                    if (y - dY > 0) {
                        int nextEnter = text.indexOf('\n', pointer);
                        int prevEnter = text.lastIndexOf('\n', pointer);
                        if (prevEnter == pointer)
                            prevEnter = text.lastIndexOf('\n', pointer - 1);

                        if (nextEnter >= 0) {
                            int nextRowEnter = text.indexOf('\n', nextEnter + 1);
                            if (nextRowEnter >= 0) {
                                if (nextRowEnter >= nextEnter + (pointer - prevEnter)) {
                                    return (nextEnter + (pointer - prevEnter));
                                } else {
                                    return nextRowEnter;
                                }
                            } else {
                                if (nextRowEnter < 0 && text.length() <= nextEnter + (pointer - prevEnter)) {
                                    return text.length();
                                }
                                return (nextEnter + (pointer - prevEnter));
                            }
                        } else {
                            return text.length();
                        }
                    } else {
                        int prevEnter = text.lastIndexOf('\n', pointer);
                        if (prevEnter == pointer)
                            prevEnter = text.lastIndexOf('\n', pointer - 1);

                        if (prevEnter >= 0) {
                            int prevRowEnter = text.lastIndexOf('\n', prevEnter - 1);
                            if (prevRowEnter >= 0) {
                                if (prevEnter >= prevRowEnter + (pointer - prevEnter)) {
                                    return (prevRowEnter + (pointer - prevEnter));
                                } else {
                                    return prevEnter;
                                }
                            } else {
                                if (prevEnter > (pointer - prevEnter)) {
                                    return (pointer - prevEnter - 1);
                                }
                            }
                        } else {
                            return 0;
                        }
                    }
                }
                return pointer;
            }
        });
        return view;
    }
}
