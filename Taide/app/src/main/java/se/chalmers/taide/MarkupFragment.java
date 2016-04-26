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
import android.widget.Switch;

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
    private final int X_SENSIVITY = 75;
    /** End constants */

    /** Start private variables */
    private int startPos = 0, endPos = 0, fragmentHeight = 0, fragmentWidth = 0, leftMostPointer = 0;
    private boolean marked = false;
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
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        startPos = TEXT_AREA.getSelectionStart();

                        ACTIVE_POINTERS.add(event.getActionIndex(), new Pointer(event.getX(), event.getY()));
                        if (getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex())) != Area.CENTER) {
                            //TODO: Do stuff
                        }
                        if (testHandler == null)
                            testHandler = new Handler();
                        if(getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex())) == Area.LEFT)
                            testHandler.postDelayed(leftScroll, DELAY);

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
                        Area area = getPointerArea(ACTIVE_POINTERS.get(event.getActionIndex()));
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

        if (Math.abs(x - dX) >= fragmentWidth / X_SENSIVITY) {
            return (int) (x - dX) / X_SENSIVITY;
        }
        return 0;
    }

    /*public int getYMovement(int pointer, int pointerIndex) {
        if (Math.abs(y - dY) > fragmentHeight / 10) {
            if (y - dY > 0) {
                int nextEnter = TEXT_AREA.getText().toString().indexOf('\n', pointer);
                int prevEnter = TEXT_AREA.getText().toString().lastIndexOf('\n', pointer);
                if (prevEnter == pointer)
                    prevEnter = TEXT_AREA.getText().toString().lastIndexOf('\n', pointer - 1);

                if (nextEnter >= 0) {
                    int nextRowEnter = TEXT_AREA.getText().toString().indexOf('\n', nextEnter + 1);
                    if (nextRowEnter >= 0) {
                        if (nextRowEnter >= nextEnter + (pointer - prevEnter)) {
                            return (nextEnter + (pointer - prevEnter));
                        } else {
                            return nextRowEnter;
                        }
                    } else {
                        if (nextRowEnter < 0 && TEXT_AREA.length() <= nextEnter + (pointer - prevEnter)) {
                            return TEXT_AREA.length();
                        }
                        return (nextEnter + (pointer - prevEnter));
                    }
                } else {
                    return TEXT_AREA.length();
                }
            } else {
                int prevEnter = TEXT_AREA.getText().toString().lastIndexOf('\n', pointer);
                if (prevEnter == pointer)
                    prevEnter = TEXT_AREA.getText().toString().lastIndexOf('\n', pointer - 1);

                if (prevEnter >= 0) {
                    int prevRowEnter = TEXT_AREA.getText().toString().lastIndexOf('\n', prevEnter - 1);
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
    }*/

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
            }
        } else {
            endPos = getNewPosition(endPos, pointerIndex);
            if(oldPos != endPos) {
                ACTIVE_POINTERS.get(pointerIndex).dX = ACTIVE_POINTERS.get(pointerIndex).x;
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
                break;
            case BOTTOM:
                break;
            case CENTER:
                return (getXMovement(pointerIndex) > 0)?
                        Math.min(TEXT_AREA.length(), getXMovement(pointerIndex) + position):
                        Math.max(0, getXMovement(pointerIndex) + position);
            case TOP_LEFT:
                break;
            case TOP_RIGHT:
                break;
            case BOTTOM_LEFT:
                break;
            case BOTTOM_RIGHT:
                break;
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
                    testHandler.postDelayed(this, DELAY);
                    if(!updatedDelay) {
                        updatedDelay = true;
                    }
                }
            }
            if(!updatedDelay) {
                testHandler.removeCallbacks(this);
                testHandler = null;
            }
        }
    };

    Runnable rightScroll = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                boolean updatedDelay = false;
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.RIGHT || area == Area.TOP_RIGHT || area == Area.BOTTOM_RIGHT) {
                    moveHandle(i);
                    testHandler.postDelayed(this, DELAY);
                    updatedDelay = true;
                }
            }
        }
    };

    Runnable topScroll = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                boolean updatedDelay = false;
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.TOP || area == Area.TOP_RIGHT || area == Area.TOP_LEFT) {
                    moveHandle(i);
                    testHandler.postDelayed(this, DELAY);
                    updatedDelay = true;
                }
            }
        }
    };

    Runnable bottomScroll = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ACTIVE_POINTERS.size(); i++) {
                boolean updatedDelay = false;
                Area area = getPointerArea(ACTIVE_POINTERS.get(i));
                if (area == Area.BOTTOM || area == Area.BOTTOM_RIGHT || area == Area.BOTTOM_LEFT) {
                    moveHandle(i);
                    testHandler.postDelayed(this, DELAY);
                    updatedDelay = true;
                }
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