package se.chalmers.taide;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
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
    private final double BORDER_PERCENTAGE = 0.10;
    private final double DRAWER_MARGIN = 0.01;
    private Handler LEFT_SCROLL_HANDLER = null;
    private Handler RIGHT_SCROLL_HANDLER = null;
    private Handler TOP_SCROLL_HANDLER = null;
    private Handler BOTTOM_SCROLL_HANDLER = null;
    private final List<Pointer> ACTIVE_POINTERS = new ArrayList<>();
    private final int X_SENSITIVITY = 75;
    private final int Y_SENSITIVITY = 5;
    /** End constants */

    /** Start private variables */
    private int startPos = 0, endPos = 0, fragmentHeight = 0, fragmentWidth = 0, leftMostPointer = 0;
    private boolean marked = false, yChanged = false;
    private Handler testHandler;
    /** End private variables */

    private enum Area{
        LEFT, RIGHT, TOP, BOTTOM, CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
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
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        fragmentWidth = size.x;

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TEXT_AREA = (EditText) getActivity().findViewById(R.id.editText);
                DrawerLayout drawer = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
                Area area;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        ACTIVE_POINTERS.add(event.getActionIndex(), new Pointer(event.getX(), event.getY()));
                        area = getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex()));
                        startPos = TEXT_AREA.getSelectionStart();

                        if(area == Area.LEFT || area == Area.TOP_LEFT || area == Area.BOTTOM_LEFT) {
                            if(LEFT_SCROLL_HANDLER == null) {
                                LEFT_SCROLL_HANDLER = new Handler();
                                LEFT_SCROLL_HANDLER.postDelayed(leftScroll, DELAY);
                            }
                        }
                        if(area == Area.RIGHT || area == Area.TOP_RIGHT || area == Area.BOTTOM_RIGHT) {
                            if(RIGHT_SCROLL_HANDLER == null) {
                                RIGHT_SCROLL_HANDLER = new Handler();
                                RIGHT_SCROLL_HANDLER.postDelayed(rightScroll, DELAY);
                            }
                        }
                        if(area == Area.TOP || area == Area.TOP_LEFT || area == Area.TOP_RIGHT) {
                            if(TOP_SCROLL_HANDLER == null) {
                                TOP_SCROLL_HANDLER = new Handler();
                                TOP_SCROLL_HANDLER.postDelayed(topScroll, DELAY);
                            }
                        }
                        if(area == Area.BOTTOM || area == Area.BOTTOM_LEFT || area == Area.BOTTOM_RIGHT) {
                            if(BOTTOM_SCROLL_HANDLER == null) {
                                BOTTOM_SCROLL_HANDLER = new Handler();
                                BOTTOM_SCROLL_HANDLER.postDelayed(bottomScroll, DELAY);
                            }
                        }

                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:

                        endPos = startPos;

                        ACTIVE_POINTERS.add(event.getActionIndex(), new Pointer(event.getX(), event.getY()));
                        leftMostPointer = getLeftMostPointerIndex();
                        Log.d("Pointers: ", ACTIVE_POINTERS.size() + "");

                        if (getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex())) != Area.CENTER) {
                            //TODO: Do stuff
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        ACTIVE_POINTERS.get(event.getActionIndex()).x = event.getX(event.getActionIndex());
                        ACTIVE_POINTERS.get(event.getActionIndex()).y = event.getY(event.getActionIndex());
                        area = getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex()));
                        leftMostPointer = getLeftMostPointerIndex();
                        if(area == Area.CENTER) {
                            moveHandle(event.getActionIndex());
                        }

                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        ACTIVE_POINTERS.remove(event.getActionIndex());
                        break;

                    case MotionEvent.ACTION_UP:
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        if (testHandler != null) {
                            testHandler.removeCallbacks(leftScroll);
                            testHandler = null;
                        }
                        ACTIVE_POINTERS.remove(event.getActionIndex());
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        return view;
    }

    public int getXMovement(int pointerIndex) {
        float x = ACTIVE_POINTERS.get(pointerIndex).x;
        float dX = ACTIVE_POINTERS.get(pointerIndex).dX;

        if (Math.abs(x - dX) >= fragmentWidth / X_SENSITIVITY) {
            return (int) (x - dX) / X_SENSITIVITY;
        }
        return 0;
    }

    public int getYMovement(int pointer, int pointerIndex) {
        float y = ACTIVE_POINTERS.get(pointerIndex).y;
        float dY = ACTIVE_POINTERS.get(pointerIndex).dY;
        Area area = getPointerArea(ACTIVE_POINTERS.get(pointerIndex));
        String text = TEXT_AREA.getText().toString();

        if (Math.abs(y - dY) >= fragmentHeight / Y_SENSITIVITY
                || (area != Area.CENTER && area != Area.RIGHT && area != Area.LEFT)) {
            if (y - dY > 0 || area == Area.BOTTOM || area == Area.BOTTOM_LEFT || area == Area.BOTTOM_RIGHT) {
                int nextEnter = text.indexOf('\n', pointer);
                int prevEnter = text.lastIndexOf('\n', pointer);
                if (prevEnter == pointer)
                    prevEnter = text.lastIndexOf('\n', pointer - 1);

                if (nextEnter >= 0) {
                    int nextRowEnter = text.indexOf('\n', nextEnter + 1);
                    if (nextRowEnter >= 0) {
                        if (nextRowEnter >= nextEnter + (pointer - prevEnter)) {
                            if(area == Area.CENTER) yChanged = true;
                            return (nextEnter + (pointer - prevEnter));
                        } else {
                            if(area == Area.CENTER) yChanged = true;
                            return nextRowEnter;
                        }
                    } else {
                        if (nextRowEnter < 0 && text.length() <= nextEnter + (pointer - prevEnter)) {
                            if(area == Area.CENTER) yChanged = true;
                            return TEXT_AREA.length();
                        }
                        if(area == Area.CENTER) yChanged = true;
                        return (nextEnter + (pointer - prevEnter));
                    }
                } else {
                    if(area == Area.CENTER) yChanged = true;
                    return TEXT_AREA.length();
                }
            } else if(y - dY < 0 || area == Area.TOP || area == Area.TOP_LEFT || area == Area.TOP_RIGHT){
                int prevEnter = text.lastIndexOf('\n', pointer);
                if (prevEnter == pointer)
                    prevEnter = text.lastIndexOf('\n', pointer - 1);

                if (prevEnter >= 0) {
                    int prevRowEnter = text.lastIndexOf('\n', prevEnter - 1);
                    if (prevRowEnter >= 0) {
                        if (prevEnter >= prevRowEnter + (pointer - prevEnter)) {
                            if(area == Area.CENTER) yChanged = true;
                            return (prevRowEnter + (pointer - prevEnter));
                        } else {
                            if(area == Area.CENTER) yChanged = true;
                            return prevEnter;
                        }
                    } else {
                        if (prevEnter > (pointer - prevEnter)) {
                            if(area == Area.CENTER) yChanged = true;
                            return (pointer - prevEnter - 1);
                        }
                    }
                } else {
                    if(area == Area.CENTER) yChanged = true;
                    return 0;
                }
            }

        }
        return pointer;
    }

    public Area getPointerArea (Pointer pointer) {
        float x = pointer.x, y = pointer.y;
        if(x < fragmentWidth * BORDER_PERCENTAGE) {
            if (y > fragmentHeight - fragmentHeight * BORDER_PERCENTAGE) {
                return Area.BOTTOM_LEFT;
            } else if(y < fragmentHeight * BORDER_PERCENTAGE) {
                return Area.TOP_LEFT;
            } else {
                return Area.LEFT;
            }

        } else if(x > fragmentWidth - fragmentWidth * BORDER_PERCENTAGE) {
            if (y > fragmentHeight - fragmentHeight * BORDER_PERCENTAGE) {
                return Area.BOTTOM_RIGHT;
            } else if(y < fragmentHeight * BORDER_PERCENTAGE) {
                return Area.TOP_RIGHT;
            } else {
                return Area.RIGHT;
            }

        } else if (y < fragmentHeight * BORDER_PERCENTAGE) {
            return Area.TOP;

        } else if (y > fragmentHeight - fragmentHeight * BORDER_PERCENTAGE) {
            return Area.BOTTOM;

        } else {
            return Area.CENTER;
        }
    }

    /**
     * Method to calculate which pointer is to furthest to the left
     * @return the index of the leftmost pointer, -1 if no pointers are active
     */
    public int getLeftMostPointerIndex() {
        int index = -1;
        float smallestX = fragmentWidth + 1;
        for(int i = 0; i < ACTIVE_POINTERS.size(); i++) {
            if(ACTIVE_POINTERS.get(i).x < smallestX) {
                smallestX = ACTIVE_POINTERS.get(i).x;
                index = i;
            }
        }
        return index;
    }

    /**
     * Move the selection handle associated to a pointer
     * @param pointerIndex index of the pointer
     */
    public void moveHandle(int pointerIndex) {
        int oldPos = startPos;
        if(pointerIndex == leftMostPointer) {
            startPos = getNewPosition(startPos, pointerIndex);
            if(oldPos != startPos) {
                ACTIVE_POINTERS.get(pointerIndex).dX = ACTIVE_POINTERS.get(pointerIndex).x;
                if(yChanged) {
                    ACTIVE_POINTERS.get(pointerIndex).dY = ACTIVE_POINTERS.get(pointerIndex).y;
                    yChanged = false;
                }
            }
        } else {
            oldPos = endPos;
            endPos = getNewPosition(endPos, pointerIndex);
            if(oldPos != endPos) {
                ACTIVE_POINTERS.get(pointerIndex).dX = ACTIVE_POINTERS.get(pointerIndex).x;
                if(yChanged) {
                    ACTIVE_POINTERS.get(pointerIndex).dY = ACTIVE_POINTERS.get(pointerIndex).y;
                    yChanged = false;
                }
            }
        }
        if(ACTIVE_POINTERS.size() > 1) {
            TEXT_AREA.setSelection(startPos, endPos);
        } else {
            TEXT_AREA.setSelection(startPos);
        }
    }

    public int getNewPosition(int position, int pointerIndex){
        Area area = getPointerArea(ACTIVE_POINTERS.get(pointerIndex));
        switch(area) {
            case LEFT:
                return Math.max(0, position - 1);

            case RIGHT:
                return Math.min(TEXT_AREA.length(), position + 1);

            case TOP:
                return getYMovement(position, pointerIndex);

            case BOTTOM:
                return getYMovement(position, pointerIndex);

            case CENTER:
                return getYMovement(getXMovement(pointerIndex) > 0 ?
                        Math.min(TEXT_AREA.length(), getXMovement(pointerIndex) + position) :
                        Math.max(0, getXMovement(pointerIndex) + position), pointerIndex);
            case TOP_LEFT:
                return getYMovement(Math.max(0, position - 1), pointerIndex);

            case TOP_RIGHT:
                return getYMovement(Math.min(0, position + 1), pointerIndex);

            case BOTTOM_LEFT:
                return getYMovement(Math.max(0, position - 1), pointerIndex);

            case BOTTOM_RIGHT:
                return getYMovement(Math.min(0, position + 1), pointerIndex);

        }
        return position;
    }

    Runnable leftScroll = new Runnable() {
        @Override
        public void run() {
            boolean updatedDelay = false;
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.LEFT || area == Area.TOP_LEFT || area == Area.BOTTOM_LEFT) {
                    moveHandle(i);
                    LEFT_SCROLL_HANDLER.postDelayed(this, DELAY);
                    if(!updatedDelay) {
                        updatedDelay = true;
                    }
                }
            }
            if(!updatedDelay) {
                LEFT_SCROLL_HANDLER.removeCallbacks(this);
                LEFT_SCROLL_HANDLER = null;
            }
        }
    };

    Runnable rightScroll = new Runnable() {
        @Override
        public void run() {
            boolean updatedDelay = false;
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.RIGHT || area == Area.TOP_RIGHT || area == Area.BOTTOM_RIGHT) {
                    moveHandle(i);
                    RIGHT_SCROLL_HANDLER.postDelayed(this, DELAY);
                    if(!updatedDelay) {
                        updatedDelay = true;
                    }
                }
            }
            if(!updatedDelay) {
                RIGHT_SCROLL_HANDLER.removeCallbacks(this);
                RIGHT_SCROLL_HANDLER = null;
            }
        }
    };

    Runnable topScroll = new Runnable() {
        @Override
        public void run() {
            boolean updatedDelay = false;
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.TOP || area == Area.TOP_RIGHT || area == Area.TOP_LEFT) {
                    moveHandle(i);
                    TOP_SCROLL_HANDLER.postDelayed(this, DELAY);
                    if(!updatedDelay) {
                        updatedDelay = true;
                    }
                }
            }
            if(!updatedDelay) {
                TOP_SCROLL_HANDLER.removeCallbacks(this);
                TOP_SCROLL_HANDLER = null;
            }
        }
    };

    Runnable bottomScroll = new Runnable() {
        @Override
        public void run() {
            boolean updatedDelay = false;
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.BOTTOM || area == Area.BOTTOM_RIGHT || area == Area.BOTTOM_LEFT) {
                    moveHandle(i);
                    BOTTOM_SCROLL_HANDLER.postDelayed(this, DELAY);
                    if(!updatedDelay) {
                        updatedDelay = true;
                    }
                }
            }
            if(!updatedDelay) {
                BOTTOM_SCROLL_HANDLER.removeCallbacks(this);
                BOTTOM_SCROLL_HANDLER = null;
            }
        }
    };

    /**
     * Simple class to group pointer information
     */
    public class Pointer {
        float x, y, dX, dY;
        Area lastArea;
        public Pointer(float x, float y) {
            this.x = x;
            this.y = y;
            dY = y;
            dX = x;
            lastArea = getPointerArea(this);
        }
    }
}