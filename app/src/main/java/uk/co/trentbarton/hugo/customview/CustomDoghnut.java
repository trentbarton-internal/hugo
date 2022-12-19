package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tools.Metrics;

public class CustomDoghnut {

    private int vehicleColour;
    private Paint backgroundPaint;

    public CustomDoghnut(int busColour){
        vehicleColour = busColour;
        init();
    }

    public CustomDoghnut(String hexCodeBusColour){
        vehicleColour = Color.parseColor(hexCodeBusColour);
        init();
    }

    private void init(){
        backgroundPaint = new Paint();
        backgroundPaint.setColor(vehicleColour);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(Metrics.densityPixelsToPixels(5));
    }

    public Bitmap getBitmap(Context context) {

        Bitmap backgroundBitmap = Bitmap.createBitmap(Metrics.densityPixelsToPixels(40), Metrics.densityPixelsToPixels(40), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(backgroundBitmap);
        canvas.drawCircle(Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(10), backgroundPaint);
        return backgroundBitmap;
    }
}
