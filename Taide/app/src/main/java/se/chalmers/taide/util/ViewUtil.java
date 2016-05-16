package se.chalmers.taide.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Matz on 2016-05-11.
 */
public class ViewUtil {

    private static View previousFocus;

    /**
     * Hides the soft keyboard and keeps track of the element with focus, to be able to use
     * it for the showSoftKeyboard(Activity, View) method.
     * @param activity The activity the changes apply in
     */
    public static void hideSoftKeyboardTemporary(Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        previousFocus = activity.getCurrentFocus();
        inputMethodManager.hideSoftInputFromWindow(previousFocus.getWindowToken(), 0);
    }

    /**
     * Displays the soft keyboard and focuses the given view. If no view is given, the ViewUtil
     * tries to determine which View element that last had focus. If this view cannot be found
     * either, the soft keyboard will remain hidden.
     * @param activity The activity the changes applies to
     * @param focusView The view to focus, or null to restore focus to the previous view.
     */
    public static void showSoftKeyboard(Activity activity, View focusView){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(focusView != null || previousFocus != null) {
            inputMethodManager.showSoftInput(focusView!=null ? focusView : previousFocus, 0);
        }
    }

    public static Position getPositionOnScreen(View view){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Position(location[0], location[1]);
    }


    public static class Position{
        private int x;
        private int y;
        public Position(int x, int y){
            this.x = x;
            this.y = y;
        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
    }
}
