package se.chalmers.taide.util;

import android.content.res.Resources;

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
     * The physical screen density in DPI as reported by the system
     */
    private static final double SYSTEM_REPORTED_PHYSICAL_DPI = Resources.getSystem().getDisplayMetrics().xdpi;


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



    //********************************************************//
    // DEVICE-INDEPENDENT physical pixel conversion functions //
    //********************************************************//

    // These functions return the same value regardless of what device they are run on, but they
    // require the caller to pass a physical DPI as an argument.

    /**
     * Converts dp to physical pixels.
     * @param dp The length in dp
     * @param physicalDPI The physical DPI of the screen
     * @return The length in pixels
     */
    public static double dpToPixels(double dp, double physicalDPI) {
        return physicalDPI * dp / DP_PER_INCH;
    }

    /**
     * Converts physical pixels to dp.
     * @param pixels The length in pixels
     * @param physicalDPI The physical DPI of the screen
     * @return The length in dp
     */
    public static double pixelsToDp(double pixels, double physicalDPI) {
        return DP_PER_INCH * pixels / physicalDPI;
    }

    /**
     * Converts physical pixels to inches.
     * @param pixels The length in pixels
     * @param physicalDPI The physical DPI of the screen
     * @return The length in inches
     */
    public static double pixelsToInches(double pixels, double physicalDPI) {
        return pixels / physicalDPI;
    }

    /**
     * Converts inches to physical pixels.
     * @param inches The length in inches
     * @param physicalDPI The physical DPI of the screen
     * @return The length in pixels
     */
    public static double inchesToPixels(double inches, double physicalDPI) {
        return inches * physicalDPI;
    }

    /**
     * Converts physical pixels to centimeters.
     * @param pixels The length in pixels
     * @param physicalDPI The physical DPI of the screen
     * @return The length in centimeters
     */
    public static double pixelsToCentimeters(double pixels, double physicalDPI) {
        return inchesToCentimeters(pixelsToInches(pixels, physicalDPI));
    }

    /**
     * Converts centimeters to physical pixels.
     * @param centimeters The length in centimeters
     * @param physicalDPI The physical DPI of the screen
     * @return The length in pixels
     */
    public static double centimetersToPixels(double centimeters, double physicalDPI) {
        return inchesToPixels(centimetersToInches(centimeters), physicalDPI);
    }



    //******************************************************//
    // DEVICE-DEPENDENT physical pixel conversion functions //
    //******************************************************//

    // The _device suffix indicates that the function's return value is dependent on the device on
    // which it is run: These functions use SYSTEM_REPORTED_PHYSICAL_DPI, so we trust them if and
    // only if we trust the system to report the correct physical DPI.

    /**
     * Converts dp to physical pixels, trusting the physical DPI as reported by the system.
     * @param dp The length in dp
     * @return The length in pixels
     */
    public static double dpToPixels_device(double dp) {
        return SYSTEM_REPORTED_PHYSICAL_DPI * dp / DP_PER_INCH;
    }

    /**
     * Converts physical pixels to dp, trusting the physical DPI as reported by the system.
     * @param pixels The length in pixels
     * @return The length in dp
     */
    public static double pixelsToDp_device(double pixels) {
        return DP_PER_INCH * pixels / SYSTEM_REPORTED_PHYSICAL_DPI;
    }

    /**
     * Converts physical pixels to inches, trusting the physical DPI as reported by the system.
     * @param pixels The length in pixels
     * @return The length in inches
     */
    public static double pixelsToInches_device(double pixels) {
        return pixels / SYSTEM_REPORTED_PHYSICAL_DPI;
    }

    /**
     * Converts inches to physical pixels, trusting the physical DPI as reported by the system.
     * @param inches The length in inches
     * @return The length in pixels
     */
    public static double inchesToPixels_device(double inches) {
        return inches * SYSTEM_REPORTED_PHYSICAL_DPI;
    }

    /**
     * Converts physical pixels to centimeters, trusting the physical DPI as reported by the system.
     * @param pixels The length in pixels
     * @return The length in centimeters
     */
    public static double pixelsToCentimeters_device(double pixels) {
        return inchesToCentimeters(pixelsToInches(pixels, SYSTEM_REPORTED_PHYSICAL_DPI));
    }

    /**
     * Converts centimeters to physical pixels, trusting the physical DPI as reported by the system.
     * @param centimeters The length in centimeters
     * @return The length in pixels
     */
    public static double centimetersToPixels_device(double centimeters) {
        return inchesToPixels(centimetersToInches(centimeters), SYSTEM_REPORTED_PHYSICAL_DPI);
    }
}
