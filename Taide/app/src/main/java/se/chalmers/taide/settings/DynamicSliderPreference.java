package se.chalmers.taide.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import net.jayschwa.android.preference.SliderPreference;

/**
 * Created by alling on 2016-05-10.
 */
public class DynamicSliderPreference extends SliderPreference {
    private TextView messageView;
    // interpret() converts the slider value (a double between 0 and 1) into the actual value of the
    // preference. This means one can use any linear, exponential or other interpretation.
    //
    // stringify() creates a readable string to display above the slider (depending on what
    // preference the slider represents etc). The default version declared here intentionally
    // returns the empty string, meaning the slider value will not be shown:
    private SliderValueInterpreter interpreter = new SliderValueInterpreter() {
        @Override
        public String stringify(double sliderValue) { return ""; }
        @Override
        public double interpret(double sliderValue) { return sliderValue; }
    };

    public DynamicSliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterpreter(SliderValueInterpreter svi) {
        interpreter = svi;
    }

    @Override
    protected View onCreateDialogView() {
        mSeekBarValue = (int) (mValue * SEEKBAR_RESOLUTION);
        final View view = super.onCreateDialogView();
        messageView = (TextView) view.findViewById(net.jayschwa.android.preference.R.id.slider_preference_message);
        final SeekBar seekbar = (SeekBar) view.findViewById(net.jayschwa.android.preference.R.id.slider_preference_seekbar);
        seekbar.setMax(SEEKBAR_RESOLUTION);
        seekbar.setProgress(mSeekBarValue);
        updateDialogMessage(seekbar.getProgress());
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    DynamicSliderPreference.this.mSeekBarValue = progress;
                    updateDialogMessage(progress);
                }
            }
        });
        return view;
    }

    protected void updateDialogMessage(int progress) {
        final String message = interpreter.stringify(progressToSliderValue(progress));
        messageView.setText(message);
        ((View) messageView.getParent()).invalidate();
    }

    /**
     * Converts the integer progress value into a fraction between 0 and 1.
     * For example: 5760 is converted to 0.576 if SEEKBAR_RESOLUTION is 10000.
     * @param progress
     * @return progress as a fraction of the seekbar length.
     */
    public static double progressToSliderValue(final int progress) {
        return (double) progress / (double) SEEKBAR_RESOLUTION;
    }

    public interface SliderValueInterpreter {
        String stringify(double sliderValue);
        double interpret(double sliderValue);
    }
}
