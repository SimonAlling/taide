package se.chalmers.taide;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MarkupFragment extends Fragment {

    /** Start constants */
    private final int DELAY = 100; //TODO: Replace with settings for sensitivity
    private EditText TEXT_AREA;
    private final double LEFT_BORDER_PERCENTAGE = 0.10;
    private final double RIGHT_BORDER_PERCENTAGE = 0.10;
    private final double TOP_BORDER_PERCENTAGE = 0.10;
    private final double BOTTOM_BORDER_PERCENTAGE = 0.10;
    private Handler LEFT_SCROLL_HANDLER = null;
    private Handler RIGHT_SCROLL_HANDLER = null;
    private Handler TOP_SCROLL_HANDLER = null;
    private Handler BOTTOM_SCROLL_HANDLER = null;
    private final List<Integer> ACTIVE_POINTERS = new ArrayList<>();
    /** End constants */

    /** Start private variables */
    private int startPos = 0, endPos = 0, fragmentHeight = 0;
    private float dX0 = 0, dX1 = 0, dY0 = 0, dY1 = 0;
    private boolean marked = false, pointer0Left = true;
    /** End private variables */

    private enum Area{
        LEFT, RIGHT, TOP, BOTTOM, CENTER
    }

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
                EditText TEXT_AREA = (EditText) getActivity().findViewById(R.id.editText);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        startPos = TEXT_AREA.getSelectionStart();
                        dX0 = event.getX();
                        dY0 = event.getY();
                        marked = false;
                        if (!ACTIVE_POINTERS.contains(event.getActionIndex())) {
                            ACTIVE_POINTERS.add(event.getActionIndex());
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        dX0 = event.getX(0);
                        dY0 = event.getY(0);
                        dX1 = event.getX(1);
                        dY1 = event.getY(1);

                        if (dX0 - dX1 < 0) {
                            pointer0Left = true;
                        } else {
                            pointer0Left = false;
                        }
                        if (!marked)
                            endPos = startPos;
                        if (!ACTIVE_POINTERS.contains(event.getActionIndex())) {
                            ACTIVE_POINTERS.add(event.getActionIndex());
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() > 1) {
                            float x0 = event.getX(0);
                            float x1 = event.getX(1);
                            int maxLength = TEXT_AREA.getText().length();
                            if (pointer0Left) {
                                int newStartPos = getNewHandlePosX(startPos, x0, dX0, maxLength);
                                int newEndPos = getNewHandlePosX(endPos, x1, dX1, maxLength);
                                if (newStartPos <= newEndPos) {
                                    TEXT_AREA.setSelection(newStartPos, newEndPos);

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
                                int newStartPosY = getNewHandlePosY(TEXT_AREA.getText().toString(), startPos, y0, dY0);
                                int newEndPosY = getNewHandlePosY(TEXT_AREA.getText().toString(), endPos, y1, dY1);
                                if (newStartPosY <= newEndPosY) {
                                    TEXT_AREA.setSelection(newStartPosY, newEndPosY);

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
                                    TEXT_AREA.setSelection(newStartPos, newEndPos);

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
                                int newStartPosY = getNewHandlePosY(TEXT_AREA.getText().toString(), startPos, y1, dY1);
                                int newEndPosY = getNewHandlePosY(TEXT_AREA.getText().toString(), endPos, y0, dY0);
                                if (newStartPosY <= newEndPosY) {
                                    TEXT_AREA.setSelection(newStartPosY, newEndPosY);

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
                                int maxLength = TEXT_AREA.getText().length();
                                int newPos = getNewHandlePosX(startPos, x, dX0, maxLength);
                                TEXT_AREA.setSelection(newPos);
                                if (newPos != startPos) {
                                    startPos = newPos;
                                    dX0 = x;
                                }
                                float y = event.getY();
                                int newPosY = getNewHandlePosY(TEXT_AREA.getText().toString(), startPos, y, dY0);
                                TEXT_AREA.setSelection(newPosY);
                                if (newPosY != startPos) {
                                    startPos = newPosY;
                                    dY0 = y;
                                }
                            } else {
                                float x0 = event.getX(0);
                                int maxLength = TEXT_AREA.getText().length();
                                if (pointer0Left) {
                                    int newStartPos = getNewHandlePosX(startPos, x0, dX0, maxLength);
                                    if (newStartPos <= endPos) {
                                        TEXT_AREA.setSelection(newStartPos, endPos);

                                        if (newStartPos != startPos) {
                                            startPos = newStartPos;
                                            dX0 = x0;
                                        }
                                    }
                                } else {
                                    int newEndPos = getNewHandlePosX(endPos, x0, dX0, maxLength);
                                    if (startPos <= newEndPos) {
                                        TEXT_AREA.setSelection(startPos, newEndPos);

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
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (marked)
                            TEXT_AREA.setSelection(startPos, endPos);
                        if (ACTIVE_POINTERS.contains(event.getActionIndex()))
                            ACTIVE_POINTERS.remove(event.getActionIndex());
                        break;

                    case MotionEvent.ACTION_UP:
                        if (marked)
                            TEXT_AREA.setSelection(startPos, endPos);
                        if (ACTIVE_POINTERS.contains(event.getActionIndex()))
                            ACTIVE_POINTERS.remove(event.getActionIndex());
                        break;

                    default:
                        return false;
                }
                if (pointerInArea(Area.LEFT) && LEFT_SCROLL_HANDLER != null) {
                    contScroll(Area.LEFT);
                } else if (pointerInArea(Area.RIGHT) && RIGHT_SCROLL_HANDLER != null) {
                    contScroll(Area.RIGHT);
                }
                return true;
            }

            public int getNewHandlePosX(int pointer, float x, float dX, int maxLength) {
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                int width = size.x;
                if (Math.abs(x - dX) >= width / 75) {
                    if (x - dX < 0 && pointer > 0) {
                        return Math.max((pointer - (int) Math.abs((x - dX) / (width / 75))), 0);
                    } else if (x - dX > 0 && pointer < maxLength) {
                        return Math.min((pointer + (int) Math.abs((x - dX) / (width / 75))), maxLength);
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

            /** Starts a continuous scrolling towards a given direction.
             * @param direction The direction to continuously scroll towards. Sending a CENTER here does nothing.
             * @return The handler issued for the continuous scroll. Null if asked to scroll towards CENTER
             */
            private Handler contScroll(Area direction) {
                Handler scrollHandler = new Handler();
                switch (direction) {
                    case LEFT:
                        LEFT_SCROLL_HANDLER.removeCallbacks(scrollLeftAction);
                        scrollHandler.postDelayed(scrollLeftAction, DELAY);
                        break;
                    case RIGHT:
                        RIGHT_SCROLL_HANDLER.removeCallbacks(scrollRightAction);
                        scrollHandler.postDelayed(scrollRightAction, DELAY);
                        break;
                    case TOP:
                        break;
                    case BOTTOM:
                        break;
                    default:
                        return null;
                }
                return scrollHandler;
            }

            /** Start Runnables */
            private final Runnable scrollRightAction = new Runnable() {
                @Override
                public void run() {
                    if (pointerInArea(Area.RIGHT)) {
                        int end = TEXT_AREA.getSelectionEnd();
                        if (marked) {
                            int start = TEXT_AREA.getSelectionStart();
                            TEXT_AREA.setSelection(start, Math.min(end + 1, TEXT_AREA.length()));
                        } else {
                            TEXT_AREA.setSelection(Math.min(end + 1, TEXT_AREA.length()));
                        }
                        RIGHT_SCROLL_HANDLER.postDelayed(this, DELAY);
                    } else {
                        RIGHT_SCROLL_HANDLER = null;
                    }
                }
            };

            private final Runnable scrollLeftAction = new Runnable() {
                @Override
                public void run() {
                    if (pointerInArea(Area.LEFT)) {
                        int start = TEXT_AREA.getSelectionStart();
                        if (marked) {
                            int end = TEXT_AREA.getSelectionEnd();
                            TEXT_AREA.setSelection(Math.max(start - 1, 0), end);
                        } else {
                            TEXT_AREA.setSelection(Math.max(start - 1, 0));
                        }
                        LEFT_SCROLL_HANDLER.postDelayed(this, DELAY);
                    } else {
                        LEFT_SCROLL_HANDLER = null;
                    }
                }
            };
            /** End Runnables */

            /** Checks if there is a pointer present in one of the five areas within the markup fragment
             * @param area the area we want to check whatever or not there is a pointer in
             * @return
             */
            private boolean pointerInArea(Area area) {
                Log.d("MarkupFragment", "Pointers active: " + ACTIVE_POINTERS.size());
                if (ACTIVE_POINTERS.size() == 0) {
                    return false;
                }
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                int width = size.x;
                int height = fragmentHeight;


                switch (area) {
                    case LEFT:
                        if ((dX0 < width * LEFT_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(0))
                                || (dX1 < width * LEFT_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(1)))
                            return true;
                        break;
                    case RIGHT:
                        if ((dX0 > width - width * RIGHT_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(0))
                                || (dX1 > width - width * RIGHT_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(1)))
                            return true;
                        break;
                    case TOP:
                        if ((dX0 > height - height * BOTTOM_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(0))
                                || (dX1 > height - height * BOTTOM_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(1)))
                            return true;
                        break;
                    case BOTTOM:
                        if ((dX0 < height * TOP_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(0))
                                || (dX1 < height * BOTTOM_BORDER_PERCENTAGE && ACTIVE_POINTERS.contains(1)))
                            return true;
                        break;
                    case CENTER:
                        if ((dX0 > width * LEFT_BORDER_PERCENTAGE
                                && dX0 < width - width * RIGHT_BORDER_PERCENTAGE
                                && dX0 > height * TOP_BORDER_PERCENTAGE
                                && dX0 < height - height * BOTTOM_BORDER_PERCENTAGE
                                && ACTIVE_POINTERS.contains(0))
                                && (dX1 > width * LEFT_BORDER_PERCENTAGE
                                && dX1 < width - width * RIGHT_BORDER_PERCENTAGE
                                && dX1 > height * TOP_BORDER_PERCENTAGE
                                && dX1 < height - height * BOTTOM_BORDER_PERCENTAGE
                                && ACTIVE_POINTERS.contains(1)))
                            return true;
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return view;
    }
}
