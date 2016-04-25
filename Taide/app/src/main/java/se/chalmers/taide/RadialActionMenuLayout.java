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

/**
 * Created by Matz on 2016-04-08.
 */
public class RadialActionMenuLayout extends RelativeLayout{

    private static final int DEFAULT_NBR_OF_BUTTONS = 3;
    private static final int DEFAULT_ANIMATION_DURATION = 100;
    private static final int DEFAULT_SELECTION_ANIMATION_DURATION = 200;
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

    private View mainView;
    private Button mainButton;

    private Alignment alignment;
    private View[] buttons;
    private OnActionButtonTriggeredListener[] buttonListeners;
    private int animationDuration;
    private int selectionAnimationDuration;
    private int buttonColor;


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
        generateButtons(attributeData.getInt(R.styleable.RadialActionMenuLayout_buttonCount, DEFAULT_NBR_OF_BUTTONS));
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
                    if (!activateButton(event.getRawX(), event.getRawY())) {
                        hide();
                    }
                }
                return false;
            }
        });
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

    public void setButtonCount(int buttonCount) {
        generateButtons(buttonCount);
        invalidate();
        requestLayout();
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

    public void setButtonText(int buttonIndex, String text) {
        if (buttonIndex >= 0 && buttonIndex < buttons.length) {
            ((Button) buttons[buttonIndex].findViewById(R.id.radialButton)).setText(text);
            invalidate();
            requestLayout();
        }
    }

    public void setButtonTexts(String[] texts) {
        if (texts.length != buttons.length) {
            throw new IllegalArgumentException("Invalid amount of labels, "+texts.length+" given but "+buttons.length+" required");
        }

        for (int i = 0; i < texts.length; i++) {
            ((Button) buttons[i].findViewById(R.id.radialButton)).setText(texts[i]);
        }
        invalidate();
        requestLayout();
    }

    public void setAction(int buttonIndex, OnActionButtonTriggeredListener listener) {
        if (buttonIndex >= 0 && buttonIndex < buttonListeners.length) {
            buttonListeners[buttonIndex] = listener;
        }
    }

    public void setActionForAll(OnActionButtonTriggeredListener listener) {
        for (int i = 0; i < buttonListeners.length; i++) {
            buttonListeners[i] = listener;
        }
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
                ((GradientDrawable) newButtons[i].findViewById(R.id.radialButton).getBackground()).setColor(getResources().getColor(buttonColor));
            } else {
                View view = getCircleButton(inflater, parent, "");
                view.setVisibility(View.GONE);
                newButtons[i] = view;
            }
        }

        this.buttons = newButtons;
        this.buttonListeners = newButtonListeners;
    }

    private void show() {
        final double childButtonDistance = mainButton.getWidth()/2 * CHILD_BUTTON_DISTANCE_FACTOR; // divide by 2 to get mother button radius
        // The angle between the lowermost and the uppermost button:
        final double totalAngle = alignment == Alignment.LEFT
                          ?  LEFT_UPPERMOST_BUTTON_ANGLE -  LEFT_LOWERMOST_BUTTON_ANGLE
                          : RIGHT_UPPERMOST_BUTTON_ANGLE - RIGHT_LOWERMOST_BUTTON_ANGLE;
        // The angle between two adjacent buttons:
        final double angleChange = totalAngle / (buttons.length-1);
        // The angle of the first button in clockwise order:
        // Note that 0 deg is an angle pointing to the right and angles increase CLOCKWISE!
        final double startAngle = alignment == Alignment.LEFT ? 0 - LEFT_UPPERMOST_BUTTON_ANGLE : 180 + RIGHT_LOWERMOST_BUTTON_ANGLE;
        for (int i = 0; i < buttons.length; i++) {
            double angle = startAngle + i*angleChange;
            double radians = Math.toRadians(angle);
            float x = (float) (childButtonDistance * Math.cos(radians));
            float y = (float) (childButtonDistance * Math.sin(radians));
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].animate().setDuration(animationDuration).translationX(x).translationY(y).start();
        }
    }

    private boolean activateButton(double x, double y) {
        for (int i = 0; i < buttons.length; i++){
            int[] location = new int[2];
            View view = buttons[i].findViewById(R.id.radialButton);
            view.getLocationOnScreen(location);
            if (x > location[0] && x < location[0]+view.getWidth() && y > location[1] && y < location[1]+view.getHeight()) {
                final View activeButton = buttons[i];
                final float scaleDiff = 1.2f;
                final float translateX = ((float) view.getWidth())*(scaleDiff-1.0f)/2.0f;
                final float translateY = ((float) view.getHeight())*(scaleDiff-1.0f)/2.0f;
                activeButton.animate().setDuration(selectionAnimationDuration).scaleX(scaleDiff).scaleY(scaleDiff).
                        translationX(activeButton.getTranslationX() - translateX).translationY(activeButton.getTranslationY()-translateY*2).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        activeButton.animate().setDuration(0).scaleX(1f).scaleY(1f).translationX(0).translationY(0).start();
                        hide();
                    }
                }).start();
                if (buttonListeners[i] != null) {
                    buttonListeners[i].actionButtonTriggered(i);
                }
                return true;
            }
        }

        return false;
    }

    private void hide() {
        for (View button : buttons) {
            button.animate().setDuration(animationDuration).translationX(0f).translationY(0f).start();
            button.setVisibility(View.GONE);
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
