package riderz.team10.ecse321.com.riderzpassengers.assets.convertor;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Display {
    /**
     * Converts DP to PX.
     * @param resource An Android Resources object
     * @param dp DP to convert to PX
     * @return An integer representing a PX
     */
    public static int dpToPX(Resources resource, int dp) {
        float scale = resource.getDisplayMetrics().density;
        return ((int) (dp * scale + 0.5f));
    }

    /**
     * Converts PX to DP.
     * @param resource An Android Resources object
     * @param px PX to convert to DP
     * @return An integer representing a DP
     */
    public static int pxToDP(Resources resource, int px) {
        return (px / (resource.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
