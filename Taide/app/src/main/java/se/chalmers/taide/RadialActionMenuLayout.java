package se.chalmers.taide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import se.chalmers.taide.util.MathUtil;
import se.chalmers.taide.util.ViewUtil;

/**
 * Created by Matz on 2016-04-08.
 *
 * Custom class for handling a radial action menu, consisting of a main button that expands
 * several other button. These buttons can be linked to action, making this ui element
 * appropriate for fast access tools etc.
 *
 * NOTE: Be sure to call setButtons(Action[] actions) to make the action menu visible.
 */
public class RadialActionMenuLayout extends RelativeLayout{

    // The size of a button. This is not a good solution but
    // it was hard to load during runtime.
    private static final int NORMAL_BUTTON_SIZE = 90;
    private static final int NAVIGATION_BUTTON_SIZE = 60;

    // Default settable properties
    private static final int DEFAULT_ANIMATION_DURATION = 100; // ms
    private static final int DEFAULT_SELECTION_ANIMATION_DURATION = 200; // ms
    private static final String DEFAULT_MAIN_BUTTON_TEXT = "";

    private static final int NBR_OF_BUTTONS = 3;

    // Configure the distance from the center of the mother button to the center of each of its child buttons:
    // The factor is multiplied by the RADIUS of the mother button to get the final value.
    private static final double CHILD_BUTTON_DISTANCE_FACTOR = 3d;

    // Configure the angles of the thumb buttons:
    // The values are in degrees.
    // 0 is straight out from the motherbutton/screen edge toward the opposite screen edge.
    // 90 is straight up toward the top of the screen.
    private static final int LEFT_LOWERMOST_BUTTON_ANGLE = 0;
    private static final int LEFT_UPPERMOST_BUTTON_ANGLE = 90;
    private static final int RIGHT_LOWERMOST_BUTTON_ANGLE = 90;
    private static final int RIGHT_UPPERMOST_BUTTON_ANGLE = 0;

    // Configure the symmetry angle:
    // Interpret the value as described above.
    private static final int SYMMETRY_ANGLE = 45; // degrees

    // Configure how close to each other the buttons will be:
    private static final float ANGLE_BETWEEN_ADJACENT_BUTTONS = 50; // degrees

    // If true, the buttons will be placed symmetrically around the symmetry angle defined above,
    // with a spacing defined by ANGLE_BETWEEN_ADJACENT_BUTTONS.
    // Otherwise, X_LOWERMOST_BUTTON_ANGLE and X_UPPERMOST_BUTTON_ANGLE will decide the button placement.
    private static final boolean USE_SYMMETRY = false;

    //Determines the distance factor from the main button the button should be triggered. This
    //factor is multiplied with the width of the main button.
    private static final int MIN_DISTANCE_DETECTION_FACTOR = 1;
    private static final int MAX_DISTANCE_DETECTION_FACTOR = 5;

    //Runnables to handle scrolling in menus
    private final static int SCROLL_DELAY = 400;
    private final Runnable SCROLL_LEFT = new Runnable() {
        @Override
        public void run() {
            if(scrollLeftButtonDown) {
                scrollMenu(false);
                scrollButtonHandler.postDelayed(SCROLL_LEFT, SCROLL_DELAY);
            }
        }
    };
    private final Runnable SCROLL_RIGHT = new Runnable() {
        @Override
        public void run() {
            if(scrollRightButtonDown) {
                scrollMenu(true);
                scrollButtonHandler.postDelayed(SCROLL_RIGHT, SCROLL_DELAY);
            }
        }
    };


    private View mainView;
    private View mainButton;
    private View scrollLeftButton, scrollRightButton;
    private boolean scrollLeftButtonDown = false, scrollRightButtonDown = false;
    private Handler scrollButtonHandler;
    private int rotationIndex = 0;

    private Alignment alignment;
    private View[] buttons;
    private Action[] buttonActions;
    private int animationDuration;
    private int selectionAnimationDuration;
    private int buttonColor;
    private int scrollButtonColor;

    private int selectedButton = -1;

    public RadialActionMenuLayout(final Context context){
        this(context, null);
    }

    public RadialActionMenuLayout(final Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadialActionMenuLayout(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mainView = LayoutInflater.from(context).inflate(R.layout.radial_action_menu, this);

        // Init values from xml
        TypedArray attributeData = context.obtainStyledAttributes(attrs, R.styleable.RadialActionMenuLayout, 0, 0);
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.actionMenuColor, tv, false);
        buttonColor = tv.data;
        if (buttonColor <= 0) {
            buttonColor = attributeData.getResourceId(R.styleable.RadialActionMenuLayout_buttonColor, -1);
        }
        scrollButtonColor = attributeData.getResourceId(R.styleable.RadialActionMenuLayout_scrollButtonColor, -1);
        if(scrollButtonColor <= 0){
            scrollButtonColor = buttonColor;
        }
        alignment = Alignment.getAlignment(attributeData.getInt(R.styleable.RadialActionMenuLayout_menuAlignment, 1));
        animationDuration = attributeData.getInt(R.styleable.RadialActionMenuLayout_animationDuration, DEFAULT_ANIMATION_DURATION);
        selectionAnimationDuration = attributeData.getInt(R.styleable.RadialActionMenuLayout_selectionAnimationDuration, DEFAULT_SELECTION_ANIMATION_DURATION);
        String mainButtonText = attributeData.getString(R.styleable.RadialActionMenuLayout_menuTitle);
        attributeData.recycle();

        // Init main button.
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainButton = getCircleButton(inflater, (ViewGroup) mainView, mainButtonText == null ? DEFAULT_MAIN_BUTTON_TEXT : mainButtonText);

        //Pre-generate buttons
        generateButtons(NBR_OF_BUTTONS);

        //Init scroll buttons
        scrollLeftButton = getScrollButton(inflater, (ViewGroup) mainView, alignment == Alignment.LEFT ? 180 : 90);
        scrollRightButton = getScrollButton(inflater, (ViewGroup) mainView, alignment == Alignment.LEFT ? 90 : 0);

        // Init main button handler
        getConcreteButton(mainButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //Handle possible triggers
                    if (selectedButton >= 0) {
                        activateButton((selectedButton+rotationIndex)%buttons.length);
                        getConcreteButton(buttons[selectedButton]).getBackground().setAlpha(255);
                        selectedButton = -1;
                    }

                    hide();

                    //Reset scrolling
                    rotationIndex = 0;
                    scrollLeftButtonDown = false;
                    scrollRightButtonDown = false;
                    resetScrollingIfValid();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //Left scroll
                    scrollLeftButtonDown = isAboveView(scrollLeftButton, event.getRawX(), event.getRawY());
                    if(scrollLeftButtonDown && scrollButtonHandler == null){
                        getConcreteButton(scrollLeftButton).getBackground().setAlpha(200);
                        scrollButtonHandler = new Handler();
                        scrollButtonHandler.post(SCROLL_LEFT);
                    }

                    //Right scroll
                    scrollRightButtonDown = isAboveView(scrollRightButton, event.getRawX(), event.getRawY());
                    if(scrollRightButtonDown && scrollButtonHandler == null){
                        getConcreteButton(scrollRightButton).getBackground().setAlpha(200);
                        scrollButtonHandler = new Handler();
                        scrollButtonHandler.post(SCROLL_RIGHT);
                    }

                    //Clear scroll button handler if needed
                    resetScrollingIfValid();

                    //Handle visual feedback of ordinary action buttons.
                    int buttonSelection = getSelectedButton(event.getRawX(), event.getRawY());
                    if (buttonSelection != RadialActionMenuLayout.this.selectedButton) {
                        for (int i = 0; i < buttons.length; i++) {
                            getConcreteButton(buttons[i]).getBackground().setAlpha(i == buttonSelection ? 128 : 255);
                        }
                        RadialActionMenuLayout.this.selectedButton = buttonSelection;
                    }
                }
                return false;
            }
        });
    }

    private void resetScrollingIfValid(){
        if(scrollButtonHandler != null && !scrollRightButtonDown && !scrollLeftButtonDown) {
            scrollButtonHandler.removeCallbacks(SCROLL_LEFT);
            scrollButtonHandler.removeCallbacks(SCROLL_RIGHT);
            scrollButtonHandler = null;
            getConcreteButton(scrollLeftButton).getBackground().setAlpha(255);
            getConcreteButton(scrollRightButton).getBackground().setAlpha(255);
        }
    }

    private static boolean isAboveView(View view, float x, float y){
        //Calc rotation factors
        float rotation = view.getRotation();
        float factorX = (rotation==0 || rotation==270 ? 0 : -1);
        float factorY = (rotation==0 || rotation==90 ? 0 : -1);

        //Calc pos and compare
        Button b = getConcreteButton(view);
        ViewUtil.Position pos = ViewUtil.getPositionOnScreen(b);
        int top = pos.getX()+(int)(factorX*b.getWidth());
        int left = pos.getY()+(int)(factorY*b.getHeight());
        return x>=top && x<=top+b.getWidth() && y>=left && y<=left+b.getHeight();
    }

    private static Button getConcreteButton(View view){
        if(view instanceof Button){
            return (Button)view;
        }else {
            View button = view.findViewById(R.id.radialButton);
            if(button != null && button instanceof Button){
                return (Button)button;
            }else{
                button = view.findViewById(R.id.arrowButton);
                if(button != null && button instanceof Button){
                    return (Button)button;
                }
            }
        }

        throw new IllegalArgumentException("The given view has no recognizable buttons attached to it");
    }

    private void generateButtons(int buttonCount) {
        View[] newButtons = new View[buttonCount];
        Action[] newButtonActions = new Action[buttonCount];
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = (ViewGroup) mainView;
        for (int i = 0; i < buttonCount; i++) {
            if (this.buttons != null && i < this.buttons.length){
                newButtons[i] = this.buttons[i];
                newButtonActions[i] = this.buttonActions[i];

                // Update color:
                Button b = getConcreteButton(newButtons[i]);
                if(b.getBackground() instanceof GradientDrawable) {
                    ((GradientDrawable) b.getBackground()).setColor(getResources().getColor(buttonColor));
                }
            } else {
                View view = getCircleButton(inflater, parent, "");
                view.setVisibility(View.GONE);
                newButtons[i] = view;
            }
        }

        this.buttons = newButtons;
        this.buttonActions = newButtonActions;
    }

    private View getScrollButton(LayoutInflater inflater, ViewGroup parent, int rotationAngle){
        //Create element
        View arrowButton = inflater.inflate(R.layout.arrow_button, null);
        if(parent != null) {
            parent.addView(arrowButton);
        }
        //Set color
        Button b = getConcreteButton(arrowButton);
        if(b.getBackground() instanceof GradientDrawable){
            ((GradientDrawable)b.getBackground()).setColor(getResources().getColor(scrollButtonColor));
        }
        //Set props
        arrowButton.setRotation(rotationAngle);
        arrowButton.setVisibility(View.GONE);
        return arrowButton;
    }

    private View getCircleButton(LayoutInflater inflater, ViewGroup parent, String text) {
        View view = inflater.inflate(R.layout.radial_button, null);
        if(parent != null) {
            parent.addView(view);
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.addRule(alignment == Alignment.RIGHT ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        view.setLayoutParams(params);

        Button button = getConcreteButton(view);
        button.setText(text);
        if (button.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) button.getBackground()).setColor(getResources().getColor(buttonColor));
        }

        return view;
    }

    private int getSelectedButton(double x, double y) {
        //Retrieve the location of the main button
        Button concreteButton = getConcreteButton(mainButton);
        ViewUtil.Position p = ViewUtil.getPositionOnScreen(getConcreteButton(mainButton));
        int mainButtonX = p.getX()+concreteButton.getWidth()/2;
        int mainButtonY = p.getY()+concreteButton.getHeight()/2;

        //Calculate the angle towards the event
        double angle = MathUtil.getAngle(mainButtonX, mainButtonY, x, y);
        double startAngle = MathUtil.convertAngleIntoNormalRange(getStartAngle(alignment, buttons.length));
        double angleChange = getAngleChange(alignment, getNbrOfVisibleButtons());

        //If the range (startAngle to (startAngle+angleChange*steps)) passes the rightmost of the
        //x-axis, the angle might be translated +-360 degrees. Therefore we have to translate it back.
        if(angle<startAngle && angleChange>0){
            angle += 360;
        }else if(angle>startAngle && angleChange<0){
            angle -= 360;
        }

        //Check that it is in the correct interval
        if((angleChange>=0 && angle >= startAngle && angle <= startAngle+angleChange*buttons.length) ||
            (angleChange<0 && angle <= startAngle && angle >= startAngle+angleChange*buttons.length)){
            int index = (int)((angle-startAngle)/angleChange);
            //Check that it is far/close enough.
            if(index<getNbrOfVisibleButtons() &&
               MathUtil.distanceIsGreaterThan(concreteButton.getWidth() * MIN_DISTANCE_DETECTION_FACTOR, mainButtonX, mainButtonY, x, y) &&
               MathUtil.distanceIsSmallerThan(concreteButton.getWidth() * MAX_DISTANCE_DETECTION_FACTOR, mainButtonX, mainButtonY, x, y)){
                return index;
            }
        }

        return -1;
    }

    private int getNbrOfVisibleButtons(){
        return Math.min(buttons.length, NBR_OF_BUTTONS);
    }

    private void activateButton(int index){
        if(index<buttonActions.length && index>=0) {
            //Perform action
            if (buttonActions[index] != null) {
                buttonActions[index].perform();
            }
        } else{
            Log.w("RadialActionMenuLayout", "Tried to activate a non-existant button!");
        }
    }

    private void scrollMenu(boolean clockwise){
        int nbrOfButtons = getNbrOfVisibleButtons();
        rotationIndex += (clockwise?1:-1)%buttons.length;
        while(rotationIndex<0){
            rotationIndex += buttons.length;
        }

        for(int i = 0; i<nbrOfButtons; i++){
            setButtonAppearance(getConcreteButton(buttons[i]), buttonActions[(rotationIndex+i)%buttons.length]);
        }
        mainView.invalidate();
        mainView.requestLayout();
    }

    private void show() {
        final double childButtonDistance = getConcreteButton(mainButton).getWidth()/2 * CHILD_BUTTON_DISTANCE_FACTOR; // division by 2 to get mother button radius
        final int steps = buttons.length - 1;
        final double angleChange = getAngleChange(alignment, getNbrOfVisibleButtons()-1);
        final double startAngle = getStartAngle(alignment, steps);
        int max = getNbrOfVisibleButtons();
        for (int i = 0; i < max; i++) {
            double angle = startAngle + i*angleChange;
            double radians = Math.toRadians(angle);
            float x = (float) (childButtonDistance * Math.cos(radians));
            float y = (float) (childButtonDistance * Math.sin(radians));
            setButtonAppearance(getConcreteButton(buttons[i]), buttonActions[i]);
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].animate().setDuration(animationDuration).translationX(x).translationY(y).start();

            //Add scroll buttons
            if(i == 0 || i == max-1){
                float newX, newY;
                View scrollButton = ((i==0 && alignment==Alignment.RIGHT) || (i==max-1 && alignment==Alignment.LEFT) ? scrollRightButton : scrollLeftButton);
                scrollButton.setX(mainButton.getX()+getConcreteButton(mainButton).getX());
                scrollButton.setY(mainButton.getY()+getConcreteButton(mainButton).getY());
                if(i == 0){
                    newX = (alignment == Alignment.RIGHT ? mainView.getWidth()- NAVIGATION_BUTTON_SIZE : 0);
                    newY = mainButton.getY()+getConcreteButton(mainButton).getY()+y+(NORMAL_BUTTON_SIZE-NAVIGATION_BUTTON_SIZE)/2;
                }else{
                    newX = mainButton.getX()+getConcreteButton(mainButton).getX()+x+(NORMAL_BUTTON_SIZE-NAVIGATION_BUTTON_SIZE)/2;
                    newY = mainView.getHeight() - NAVIGATION_BUTTON_SIZE;
                }

                scrollButton.animate().setDuration(animationDuration).translationX(newX).translationY(newY).start();
            }
        }


        if(buttonActions.length > getNbrOfVisibleButtons()) {
            scrollLeftButton.setVisibility(View.VISIBLE);
            scrollRightButton.setVisibility(View.VISIBLE);
        }

        mainView.invalidate();
        mainView.requestLayout();
    }

    private void hide() {
        for (View button : buttons) {
            button.animate().setDuration(animationDuration).translationX(0f).translationY(0f).start();
            button.setVisibility(View.GONE);
        }

        scrollLeftButton.setVisibility(View.GONE);
        scrollRightButton.setVisibility(View.GONE);
    }

    private static double getAngleChange(Alignment alignment, int steps){
        // The angle between two adjacent buttons:
        // The special case for buttons.length == 1 is there because otherwise angleChange will be
        // NaN if symmetry is not used, and then we cannot support exactly one button.
        if(USE_SYMMETRY){
            return ANGLE_BETWEEN_ADJACENT_BUTTONS;
        }else{
            double totalAngle = alignment == Alignment.LEFT
                    ?  LEFT_UPPERMOST_BUTTON_ANGLE -  LEFT_LOWERMOST_BUTTON_ANGLE
                    : RIGHT_UPPERMOST_BUTTON_ANGLE - RIGHT_LOWERMOST_BUTTON_ANGLE;
            return steps > 0 ? totalAngle / steps : 0;
        }
    }

    private static double getStartAngle(Alignment alignment, int steps){
        // The angle of the first button in clockwise order:
        // Note that 0 deg is an angle pointing to the right and angles increase CLOCKWISE!
        if(USE_SYMMETRY){
            final double totalAngle = steps * ANGLE_BETWEEN_ADJACENT_BUTTONS;
            return alignment == Alignment.LEFT
                    ?   0 - SYMMETRY_ANGLE - (totalAngle/2)
                    : 180 + SYMMETRY_ANGLE - (totalAngle/2);
        }else{
            return alignment == Alignment.LEFT
                    ?   0 -  LEFT_UPPERMOST_BUTTON_ANGLE
                    : 180 + RIGHT_LOWERMOST_BUTTON_ANGLE;
        }
    }


    /**
     * Takes an array of strings and creates buttons with those labels.
     * There will be NO BUTTONS until this is called.
     * @param actions The desired actions of the buttons in clockwise order
     */
    public void setButtons(Action[] actions) {
        generateButtons(actions.length);
        for (int i = 0; i < actions.length; i++) {
            setButtonAppearance(getConcreteButton(buttons[i]), actions[i]);
        }
        this.buttonActions = actions;
        invalidate();
        requestLayout();
    }

    private void setButtonAppearance(Button b, Action action){
        if(action.getIcon() > 0) {      //Check if icon exists
            Drawable icon = getResources().getDrawable(action.getIcon());
            b.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            int padding = (NORMAL_BUTTON_SIZE -icon.getBounds().height())/2;
            b.setPadding(0, padding, 0, 0);
        } else{
            b.setText(action.getName());
        }
    }

    public void setAlignment(Alignment alignment) {
        if (alignment != null && alignment != this.alignment) {
            this.alignment = alignment;
            this.scrollLeftButton.setRotation(alignment==Alignment.LEFT ? 180 : 90);
            this.scrollRightButton.setRotation(alignment==Alignment.LEFT ? 90 : 0);
            invalidate();
            requestLayout();
        }
    }

    public Alignment getAlignment(){
        return alignment;
    }

    public int getButtonCount(){
        return buttons.length;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public int getAnimationDuration(){
        return animationDuration;
    }

    public void setSelectionAnimationDuration(int selectionAnimationDuration) {
        this.selectionAnimationDuration = selectionAnimationDuration;
    }

    public int getSelectionAnimationDuration(){
        return selectionAnimationDuration;
    }

    public void setButtonColor(int resourceId) {
        this.buttonColor = resourceId;
        generateButtons(getNbrOfVisibleButtons());
        invalidate();
        requestLayout();
    }

    public int getButtonColor(){
        return buttonColor;
    }

    public void setMenuTitle(String title) {
        getConcreteButton(mainButton).setText(title);
        invalidate();
        requestLayout();
    }

    public String getMenuTitle(){
        return getConcreteButton(mainButton).getText().toString();
    }

    public enum Alignment {
        LEFT, RIGHT;

        private static Alignment getAlignment(int index) {
            return (index == 0 ? LEFT : RIGHT);
        }
    }
}
