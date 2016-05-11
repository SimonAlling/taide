package se.chalmers.taide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import se.chalmers.taide.util.MathUtil;

/**
 * Created by Matz on 2016-04-08.
 *
 * Custom class for handling a radial action menu, consisting of a main button that expands
 * several other button. These buttons can be linked to action, making this ui element
 * appropriate for fast access tools etc.
 *
 * NOTE: Be sure to call setButtons(String[] labels) to make the action menu visible.
 */
public class RadialActionMenuLayout extends RelativeLayout{

    private static final int DEFAULT_ANIMATION_DURATION = 100; // ms
    private static final int DEFAULT_SELECTION_ANIMATION_DURATION = 200; // ms
    private static final String DEFAULT_MAIN_BUTTON_TEXT = "";

    // Configure the distance from the center of the mother button to the center of each of its child buttons:
    // The factor is multiplied by the RADIUS of the mother button to get the final value.
    private static final double CHILD_BUTTON_DISTANCE_FACTOR = 3d;

    // Configure the angles of the thumb buttons:
    // The values are in degrees.
    // 0 is straight out from the motherbutton/screen edge toward the opposite screen edge.
    // 90 is straight up toward the top of the screen.
    private static final int LEFT_LOWERMOST_BUTTON_ANGLE = -5;
    private static final int LEFT_UPPERMOST_BUTTON_ANGLE = 95;
    private static final int RIGHT_LOWERMOST_BUTTON_ANGLE = -5;
    private static final int RIGHT_UPPERMOST_BUTTON_ANGLE = 95;

    // Configure the symmetry angle:
    // Interpret the value as described above.
    private static final int SYMMETRY_ANGLE = 45; // degrees

    // Configure how close to each other the buttons will be:
    private static final float ANGLE_BETWEEN_ADJACENT_BUTTONS = 50; // degrees

    // If true, the buttons will be placed symmetrically around the symmetry angle defined above,
    // with a spacing defined by ANGLE_BETWEEN_ADJACENT_BUTTONS.
    // Otherwise, X_LOWERMOST_BUTTON_ANGLE and X_UPPERMOST_BUTTON_ANGLE will decide the button placement.
    private static final boolean USE_SYMMETRY = true;

    //Determines the distance factor from the main button the button should be triggered. This
    //factor is multiplied with the width of the main button.
    private static final int MIN_DISTANCE_DETECTION_FACTOR = 1;
    private static final int MAX_DISTANCE_DETECTION_FACTOR = 5;


    private View mainView;
    private Button mainButton;

    private Alignment alignment;
    private View[] buttons;
    private OnActionButtonTriggeredListener[] buttonListeners;
    private int animationDuration;
    private int selectionAnimationDuration;
    private int buttonColor;

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
        buttonColor = attributeData.getResourceId(R.styleable.RadialActionMenuLayout_buttonColor, -1);
        if (buttonColor < 0) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.actionMenuColor, tv, false);
            buttonColor = tv.data;
        }
        alignment = Alignment.getAlignment(attributeData.getInt(R.styleable.RadialActionMenuLayout_menuAlignment, 1));
        animationDuration = attributeData.getInt(R.styleable.RadialActionMenuLayout_animationDuration, DEFAULT_ANIMATION_DURATION);
        selectionAnimationDuration = attributeData.getInt(R.styleable.RadialActionMenuLayout_selectionAnimationDuration, DEFAULT_SELECTION_ANIMATION_DURATION);
        String mainButtonText = attributeData.getString(R.styleable.RadialActionMenuLayout_menuTitle);
        attributeData.recycle();

        // Init main button
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainButtonView = getCircleButton(inflater, (ViewGroup) mainView, mainButtonText==null ? DEFAULT_MAIN_BUTTON_TEXT : mainButtonText);

        // Init main button handler
        mainButton = (Button) mainButtonView.findViewById(R.id.radialButton);
        mainButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(selectedButton >= 0){
                        activateButton(selectedButton);
                        buttons[selectedButton].findViewById(R.id.radialButton).getBackground().setAlpha(255);
                    }
                    selectedButton = -1;
                    hide();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE){
                    int buttonSelection = getSelectedButton(event.getRawX(), event.getRawY());
                    if(buttonSelection != RadialActionMenuLayout.this.selectedButton) {
                        for (int i = 0; i<buttons.length; i++) {
                            buttons[i].findViewById(R.id.radialButton).getBackground().setAlpha(i==buttonSelection ? 128 : 255);
                        }
                        RadialActionMenuLayout.this.selectedButton = buttonSelection;
                    }
                }
                return false;
            }
        });
    }

    private void generateButtons(int buttonCount) {
        View[] newButtons = new View[buttonCount];
        OnActionButtonTriggeredListener[] newButtonListeners = new OnActionButtonTriggeredListener[buttonCount];
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout parent = (RelativeLayout) mainView;
        for (int i = 0; i < buttonCount; i++) {
            if (this.buttons != null && i < this.buttons.length){
                newButtons[i] = this.buttons[i];
                newButtonListeners[i] = this.buttonListeners[i];

                // Update color:
                Button b = (Button)newButtons[i].findViewById(R.id.radialButton);
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
        this.buttonListeners = newButtonListeners;
    }

    private View getCircleButton(LayoutInflater inflater, ViewGroup parent, String text) {
        View view = inflater.inflate(R.layout.radial_button, null);
        parent.addView(view);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.addRule(alignment == Alignment.RIGHT ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        view.setLayoutParams(params);

        Button button = ((Button) view.findViewById(R.id.radialButton));
        button.setText(text);
        if (button.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) button.getBackground()).setColor(getResources().getColor(buttonColor));
        }

        return view;
    }

    private int getSelectedButton(double x, double y) {
        //Retrieve the location of the main button
        int[] location = new int[2];
        mainButton.getLocationOnScreen(location);
        location[0] += mainButton.getWidth()/2;
        location[1] += mainButton.getHeight()/2;

        //Calculate the angle towards the event
        int steps = buttons.length-1;
        double angle = MathUtil.getAngle(location[0], location[1], x, y);
        double startAngle = MathUtil.convertAngleIntoNormalRange(getStartAngle(alignment, steps));
        double angleChange = getAngleChange(alignment, steps);

        //If the range (startAngle to (startAngle+angleChange*steps)) passes the rightmost of the
        //x-axis, the angle might be translated -360 degrees. Therefore we have to translate it back.
        if(angle<startAngle){
            angle += 360;
        }

        //Check that it is in the correct interval
        if(angle >= startAngle && angle <= startAngle+angleChange*(steps+1)){
            int index = (int)((angle-startAngle)/angleChange);
            //Check that it is far/close enough.
            if(index<buttons.length && MathUtil.distanceIsGreaterThan(mainButton.getWidth() * MIN_DISTANCE_DETECTION_FACTOR, location[0], location[1], x, y) &&
                                       MathUtil.distanceIsSmallerThan(mainButton.getWidth() * MAX_DISTANCE_DETECTION_FACTOR, location[0], location[1], x, y)){
                return index;
            }
        }

        return -1;
    }

    private void activateButton(int index){
        //Calculate values
        final View activeButton = buttons[index];
        final float scaleDiff = 1.2f;
        final float translateX = ((float) activeButton.getWidth())*(scaleDiff-1.0f)/2.0f;
        final float translateY = ((float) activeButton.getHeight())*(scaleDiff-1.0f)/2.0f;

        //Perform the animation
        activeButton.animate().setDuration(selectionAnimationDuration).scaleX(scaleDiff).scaleY(scaleDiff).
                translationX(activeButton.getTranslationX() - translateX).translationY(activeButton.getTranslationY()-translateY*2).withEndAction(new Runnable() {
            @Override
            public void run() {
                activeButton.animate().setDuration(0).scaleX(1f).scaleY(1f).translationX(0).translationY(0).start();
                hide();
            }
        }).start();

        //Notify listeners
        if (buttonListeners[index] != null) {
            buttonListeners[index].actionButtonTriggered(index);
        }
    }

    private void show() {
        final double childButtonDistance = mainButton.getWidth()/2 * CHILD_BUTTON_DISTANCE_FACTOR; // division by 2 to get mother button radius
        final int steps = buttons.length - 1;
        final double angleChange = getAngleChange(alignment, steps);
        final double startAngle = getStartAngle(alignment, steps);
        for (int i = 0; i < buttons.length; i++) {
            double angle = startAngle + i*angleChange;
            double radians = Math.toRadians(angle);
            float x = (float) (childButtonDistance * Math.cos(radians));
            float y = (float) (childButtonDistance * Math.sin(radians));
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].animate().setDuration(animationDuration).translationX(x).translationY(y).start();
        }
    }

    private void hide() {
        for (View button : buttons) {
            button.animate().setDuration(animationDuration).translationX(0f).translationY(0f).start();
            button.setVisibility(View.GONE);
        }
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
     * @param labels The desired labels of the buttons in clockwise order
     */
    public void setButtons(String[] labels) {
        generateButtons(labels.length);
        for (int i = 0; i < labels.length; i++) {
            ((Button) buttons[i].findViewById(R.id.radialButton)).setText(labels[i]);
        }
        invalidate();
        requestLayout();
    }

    public void setActionForAll(OnActionButtonTriggeredListener listener) {
        for (int i = 0; i < buttonListeners.length; i++) {
            buttonListeners[i] = listener;
        }
    }

    public void setAction(int buttonIndex, OnActionButtonTriggeredListener listener) {
        if (buttonIndex >= 0 && buttonIndex < buttonListeners.length) {
            buttonListeners[buttonIndex] = listener;
        }
    }

    public void setAlignment(Alignment alignment) {
        if (alignment != null && alignment != this.alignment) {
            this.alignment = alignment;
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
        generateButtons(buttons.length);
        invalidate();
        requestLayout();
    }

    public int getButtonColor(){
        return buttonColor;
    }

    public void setMenuTitle(String title) {
        mainButton.setText(title);
        invalidate();
        requestLayout();
    }

    public String getMenuTitle(){
        return mainButton.getText().toString();
    }

    public void setButtonLabel(int buttonIndex, String label) {
        if (buttonIndex >= 0 && buttonIndex < buttons.length) {
            ((Button) buttons[buttonIndex].findViewById(R.id.radialButton)).setText(label);
            invalidate();
            requestLayout();
        }
    }

    public interface OnActionButtonTriggeredListener {
        void actionButtonTriggered(int index);
    }

    public enum Alignment {
        LEFT, RIGHT;

        private static Alignment getAlignment(int index) {
            return (index == 0 ? LEFT : RIGHT);
        }
    }
}
