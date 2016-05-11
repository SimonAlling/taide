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



    public static boolean distanceIsGreaterThan(double maxLimit, double x1, double y1, double x2, double y2){
        return Math.pow(Math.abs(x1-x2), 2)+Math.pow(Math.abs(y1-y2), 2) > Math.pow(maxLimit, 2);
    }

    public static boolean distanceIsSmallerThan(double maxLimit, double x1, double y1, double x2, double y2){
        return Math.pow(Math.abs(x1-x2), 2)+Math.pow(Math.abs(y1-y2), 2) < Math.pow(maxLimit, 2);
    }

    public static double convertAngleIntoNormalRange(double angle){
        while(angle<0){
            angle += 360;
        }

        return angle%360;
    }

    public static double getAngle(double x1, double y1, double x2, double y2){
        final double dx = x2-x1, dy = y2-y1;
        if(dx != 0) {
            double angle = Math.toDegrees(Math.atan(dy/dx));
            if(dx<0){
                return 180 + angle;
            }else{
                return dy>0 ? angle : 360 + angle;
            }
        }else{
            return y2>y1 ? 90 : 270;
        }
    }
}

