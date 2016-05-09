package se.chalmers.taide.util;

/**
 * Created by alling on 2016-05-09.
 */
public class Units {

    /**
     * Google's reference screen density that is used to define dp (density-independent pixels)
     */
    public static final double DP_PER_INCH = 160.0;

    /**
     * The number of centimeters per inch
     */
    public static final double CM_PER_INCH = 2.54;


    /**
     * Converts inches to centimeters.
     * @param inches The length in inches
     * @return The length in centimeters
     */
    public static double inchesToCentimeters(double inches) {
        return inches * CM_PER_INCH;
    }

    /**
     * Converts centimeters to inches.
     * @param centimeters The length in centimeters
     * @return The length in inches
     */
    public static double centimetersToInches(double centimeters) {
        return centimeters / CM_PER_INCH;
    }

    /**
     * Converts inches to dp.
     * @param inches The length in inches
     * @return The length in dp
     */
    public static double inchesToDp(double inches) {
        return inches * DP_PER_INCH;
    }

    /**
     * Converts dp to inches.
     * @param dp The length in dp
     * @return The length in inches
     */
    public static double dpToInches(double dp) {
        return dp / DP_PER_INCH;
    }

    /**
     * Converts centimeters to dp.
     * @param centimeters The length in centimeters
     * @return The length in dp
     */
    public static double centimetersToDp(double centimeters) {
        return inchesToDp(centimetersToInches(centimeters));
    }

    /**
     * Converts dp to centimeters.
     * @param dp The length in dp
     * @return The length in centimeters
     */
    public static double dpToCentimeters(double dp) {
        return inchesToCentimeters(dpToInches(dp));
    }
}
