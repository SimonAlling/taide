package se.chalmers.taide.util;

/**
 * Created by alling on 2016-05-04.
 */
public abstract class SensitivityUtil {

    // The number of CHARACTERS PER CM for the min and max sensitivity slider values, respectively:
    public static final double MIN_SENS_CHARS_PER_CM = 1;
    public static final double MAX_SENS_CHARS_PER_CM = 16;
    public static final double MIN_SENS_LINES_PER_CM = 0.5;
    public static final double MAX_SENS_LINES_PER_CM = 4;

    // This controls whether the sensitivity slider is linear or exponential:
    // (Note that "exponential" here has nothing to do with acceleration. It is only the sensitivity
    // slider that's exponential.)
    public static final boolean SLIDER_IS_EXPONENTIAL = true;


    // Calculates sensitivity for a linear sensitivity slider:
    // (sliderValue is between 0 and 1, inclusively, for a real slider.)
    private static double charactersPerCentimeter_linear(double sliderValue) {
        final double sensitivityInterval = MAX_SENS_CHARS_PER_CM - MIN_SENS_CHARS_PER_CM;
        return MIN_SENS_CHARS_PER_CM + sliderValue * sensitivityInterval;
    }

    // Calculates sensitivity for an exponential sensitivity slider:
    // (sliderValue is between 0 and 1, inclusively, for a real slider.)
    private static double charactersPerCentimeter_exponential(double sliderValue) {
        // The sensitivity f(x) for a slider value of x is calculated as
        // f(x) = ae^(kx) = ae^(ln(b/a)x)
        // where a and b are the min and max sensitivity values, respectively.
        final double k = Math.log(MAX_SENS_CHARS_PER_CM / MIN_SENS_CHARS_PER_CM); // k = ln (max / min)
        return MIN_SENS_CHARS_PER_CM * Math.pow(Math.E, k * sliderValue);
    }

    /**
     * Calculates the actual on-screen sensitivity in characters/cm depending on the value of the
     * sensitivity slider.
     * @param sliderValue A double, usually between 0 and 1, representing the slider setting
     * @return The number of characters that one centimeter should correspond to
     */
    public static double charactersPerCentimeter(double sliderValue) {
        return SLIDER_IS_EXPONENTIAL ? charactersPerCentimeter_exponential(sliderValue) : charactersPerCentimeter_linear(sliderValue);
    }

    private static double linesPerCentimeter_linear(double sliderValue) {
        final double sensitivityInterval = MAX_SENS_LINES_PER_CM - MIN_SENS_LINES_PER_CM;
        return MIN_SENS_LINES_PER_CM + sliderValue * sensitivityInterval;
    }

    private static double linesPerCentimeter_exponential(double sliderValue) {
        final double k = Math.log(MAX_SENS_LINES_PER_CM / MIN_SENS_LINES_PER_CM); // k = ln (max / min)
        return MIN_SENS_LINES_PER_CM * Math.pow(Math.E, k * sliderValue);
    }

    public static double linesPerCentimeter(double sliderValue) {
        return SLIDER_IS_EXPONENTIAL ? linesPerCentimeter_exponential(sliderValue) : linesPerCentimeter_linear(sliderValue);
    }

    /**
     * Calculates the number of characters that the caret should move when the user moves their
     * finger the specified number of dp with the specified sensitivity preference.
     * @param dp The number of dp that the user moved their finger
     * @param sliderValue The value of the sensitivity preference slider, usually between 0 and 1
     * @return The number of characters that the specified distance in dp corresponds to
     */
    public static int dpToCharacters(double dp, double sliderValue) {
        // Note that we floor() the computed value here! Another option would be to round().
        return (int) Math.floor(charactersPerCentimeter(sliderValue) * Units.dpToCentimeters(dp));
    }
}
