package se.chalmers.taide.util;

/**
 * Created by alling on 2016-05-04.
 */
public abstract class MathUtil {

    /**
     * Rounds a floating point number to a specified number of decimals.
     * @param x The number to round
     * @param decimals The maximum number of decimals in the result
     * @return <code>x</code> rounded to <code>decimals</code> decimals.
     */
    public static double round(double x, int decimals) {
        final double factor = Math.pow(10, decimals); // e.g. 1000 if decimals == 3
        return Math.round(x * factor) / factor;
    }
}

