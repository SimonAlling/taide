package se.chalmers.taide.util;

/**
 * Created by alling on 2016-05-04.
 */
public abstract class SensitivityUtil {

    // The number of CHARACTERS PER CM for the min and max sensitivity slider values, respectively:
    public static final double MIN_SENS = 1;
    public static final double MAX_SENS = 16;

    // This controls whether the sensitivity slider is linear or exponential:
    // (Note that "exponential" here has nothing to do with acceleration. It is only the sensitivity
    // slider that's exponential.)
    public static final boolean SLIDER_IS_EXPONENTIAL = true;


    // Calculates sensitivity for a linear sensitivity slider:
    // (sliderValue is between 0 and 1, inclusively, for a real slider.)
    private static double calculateSensitivity_linear(double sliderValue) {
        final double sensitivityInterval = MAX_SENS - MIN_SENS;
        return MIN_SENS + sliderValue * sensitivityInterval;
    }

    // Calculates sensitivity for an exponential sensitivity slider:
    // (sliderValue is between 0 and 1, inclusively, for a real slider.)
    private static double calculateSensitivity_exponential(double sliderValue) {
        // The sensitivity f(x) for a slider value of x is calculated as
        // f(x) = ae^(kx) = ae^(ln(b/a)x)
        // where a and b are the min and max sensitivity values, respectively.
        final double k = Math.log(MAX_SENS / MIN_SENS); // k = ln (max / min)
        return MIN_SENS * Math.pow(Math.E, k * sliderValue);
    }

    /**
     * Calculates the actual on-screen sensitivity in characters/cm depending on the value of the
     * sensitivity slider.
     * @param sliderValue A double, usually between 0 and 1, representing the slider setting
     * @return The number of characters that one centimeter should correspond to.
     */
    public static double calculateSensitivity(double sliderValue) {
        return SLIDER_IS_EXPONENTIAL ? calculateSensitivity_exponential(sliderValue) : calculateSensitivity_linear(sliderValue);
    }
}
