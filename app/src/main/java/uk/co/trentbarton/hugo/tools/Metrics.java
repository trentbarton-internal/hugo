package uk.co.trentbarton.hugo.tools;

import android.content.res.Resources;

public class Metrics {

    public static int densityPixelsToPixels(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pixelsToDensityPixels(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
