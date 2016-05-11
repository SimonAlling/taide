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
    // stringifier is used to convert the slider value (a double between 0 and 1) into a readable
    // string (depending on what preference the slider represents etc). The default stringifier just
    // returns the empty string, meaning the slider value will not be shown:
    private SliderValueStringifier stringifier = new SliderValueStringifier() {
        @Override
        public String stringify(double sliderValue) { return ""; }
    };

    public DynamicSliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStringifier(SliderValueStringifier s) {
        stringifier = s;
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
        final String message = stringifier.stringify(progressToSliderValue(progress));
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

    public interface SliderValueStringifier {
        String stringify(double sliderValue);
    }
}
