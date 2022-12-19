package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class CustomBusMarker {

    private int vehicleColour;
    private Paint backgroundPaint;

    public CustomBusMarker(int busColour){
        vehicleColour = busColour;
        init();
    }

    public CustomBusMarker(String hexCodeBusColour){
        vehicleColour = Color.parseColor(hexCodeBusColour);
        init();
    }

    private void init(){
        backgroundPaint = new Paint();
        backgroundPaint.setColor(vehicleColour);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundPaint.setStrokeWidth(0.0f);
    }

    public Bitmap getBitmap(Context context) {

        Bitmap backgroundBitmap = Bitmap.createBitmap(Metrics.densityPixelsToPixels(40), Metrics.densityPixelsToPixels(40), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(backgroundBitmap);
        canvas.drawCircle(Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(18), backgroundPaint);
        Bitmap cutOut = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_cut_out);
        Bitmap busIcon = Bitmap.createScaledBitmap(cutOut, Metrics.densityPixelsToPixels(40), Metrics.densityPixelsToPixels(40), false);
        canvas.drawBitmap(busIcon, 0, 0, null);
        return backgroundBitmap;
    }

}
