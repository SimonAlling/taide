package se.chalmers.taide;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
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

    private static final String DEFAULT_NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private static final int DEFAULT_NBR_OF_BUTTONS = 3;
    private static final int DEFAULT_ANIMATION_DURATION = 400;
    private static final int DEFAULT_SELECTION_ANIMATION_DURATION = 200;
    private static final String DEFAULT_MAIN_BUTTON_TEXT = "";

    private View mainView;
    private Button mainButton;

    private Alignment alignment;
    private View[] buttons;
    private OnActionButtonTriggeredListener[] buttonListeners;
    private int animationDuration;
    private int selectionAnimationDuration;


    public RadialActionMenuLayout(final Context context){
        this(context, null);
    }

    public RadialActionMenuLayout(final Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public RadialActionMenuLayout(final Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        mainView = LayoutInflater.from(context).inflate(R.layout.radial_action_menu, this);

        //Init values from xml
        int buttonColor = -1, mainButtonText = -1;
        try {
            buttonColor = attrs.getAttributeResourceValue(DEFAULT_NAMESPACE, "buttonColor", -1);
            alignment = Alignment.getAlignment(attrs.getAttributeIntValue(DEFAULT_NAMESPACE, "menuAlignment", 1));
            generateButtons(attrs.getAttributeIntValue(DEFAULT_NAMESPACE, "buttonCount", DEFAULT_NBR_OF_BUTTONS), buttonColor);
            animationDuration = attrs.getAttributeIntValue(DEFAULT_NAMESPACE, "animationDuration", DEFAULT_ANIMATION_DURATION);
            selectionAnimationDuration = attrs.getAttributeIntValue(DEFAULT_NAMESPACE, "selectionAnimationDuration", DEFAULT_SELECTION_ANIMATION_DURATION);
            mainButtonText = attrs.getAttributeResourceValue(DEFAULT_NAMESPACE, "title", -1);
        }catch(NumberFormatException nfe){
            Log.w("RadialActionMenuLayout", "Invalid XML data: Integer value required.");
        }

        //Init main button
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainButtonView = getCircleButton(inflater, (ViewGroup)mainView, mainButtonText>=0?getResources().getString(mainButtonText):DEFAULT_MAIN_BUTTON_TEXT, buttonColor);

        //Init main button handler
        mainButton = (Button)mainButtonView.findViewById(R.id.radialButton);
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

    public void setButtonText(int buttonIndex, String text){
        if(buttonIndex>=0 && buttonIndex<buttons.length){
            ((Button)buttons[buttonIndex].findViewById(R.id.radialButton)).setText(text);
        }
    }

    public void setButtonTexts(String[] texts){
        if(texts.length != buttons.length){
            throw new IllegalArgumentException("Invalid amount of labels, "+texts.length+" given but "+buttons.length+" required");
        }

        for(int i = 0; i<texts.length; i++){
            setButtonText(i, texts[i]);
        }
    }

    public void setAction(int buttonIndex, OnActionButtonTriggeredListener listener){
        if(buttonIndex>=0 && buttonIndex<buttonListeners.length){
            buttonListeners[buttonIndex] = listener;
        }
    }

    public void setActionForAll(OnActionButtonTriggeredListener listener){
        for(int i = 0; i<buttonListeners.length; i++){
            buttonListeners[i] = listener;
        }
    }

    private View getCircleButton(LayoutInflater inflater, ViewGroup parent, String text, int color){
        View v = inflater.inflate(R.layout.radial_button, null);
        parent.addView(v);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.addRule(alignment == Alignment.RIGHT ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        v.setLayoutParams(params);

        Button b = ((Button)v.findViewById(R.id.radialButton));
        b.setText(text);
        if(color >= 0){
            ((GradientDrawable)b.getBackground()).setColor(getResources().getColor(color));
        }

        return v;
    }

    private void generateButtons(int buttonCount, int color){
        this.buttons = new View[buttonCount];
        this.buttonListeners = new OnActionButtonTriggeredListener[buttonCount];
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout parent = (RelativeLayout)mainView;
        for(int i = 0; i < buttonCount; i++){
            View v = getCircleButton(inflater, parent, "", color);
            v.setVisibility(View.GONE);
            buttons[i] = v;
        }
    }

    private void show(){
        double radius = mainButton.getWidth()*1.5d;
        double angleChange = 100d/(buttons.length-1);
        for(int i = 0; i<buttons.length; i++){
            double angle = (alignment==Alignment.LEFT?265+i*angleChange:275-i*angleChange);
            double radians = angle * Math.PI / 180d;
            float x = (float)(radius * Math.cos(radians));
            float y = (float)(radius * Math.sin(radians));
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].animate().setDuration(animationDuration).translationX(x).translationY(y).start();
        }
    }

    private boolean activateButton(double x, double y){
        for(int i = 0; i<buttons.length; i++){
            int[] location = new int[2];
            View v = buttons[i].findViewById(R.id.radialButton);
            v.getLocationOnScreen(location);
            if(x>location[0] && x<location[0]+v.getWidth() && y>location[1] && y<location[1]+v.getHeight()){
                final View activeButton = buttons[i];
                final float scaleDiff = 1.2f;
                final float translateX = ((float)v.getWidth())*(scaleDiff-1.0f)/2.0f;
                final float translateY = ((float)v.getHeight())*(scaleDiff-1.0f)/2.0f;
                activeButton.animate().setDuration(selectionAnimationDuration).scaleX(scaleDiff).scaleY(scaleDiff).
                        translationX(activeButton.getTranslationX() - translateX).translationY(activeButton.getTranslationY()-translateY*2).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        activeButton.animate().setDuration(0).scaleX(1f).scaleY(1f).translationX(0).translationY(0).start();
                        hide();
                    }
                }).start();
                if(buttonListeners[i] != null){
                    buttonListeners[i].actionButtonTriggered(i);
                }
                return true;
            }
        }

        return false;
    }

    private void hide(){
        for(View b : buttons){
            b.animate().setDuration(animationDuration).translationX(0f).translationY(0f).start();
            b.setVisibility(View.GONE);
        }
    }

    public interface OnActionButtonTriggeredListener{
        void actionButtonTriggered(int index);
    }

    private enum Alignment{
        LEFT, RIGHT;

        private static Alignment getAlignment(int index){
            return (index==0?LEFT:RIGHT);
        }
    }
}
