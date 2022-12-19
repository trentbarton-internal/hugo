package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import uk.co.trentbarton.hugo.R;

public class RefreshView extends View {

    private Paint mCirclePaint;
    private Paint mGreyBackgroundPaint, mWhiteBackgroundPaint;
    private Matrix rotator;
    private RectF oval;
    private int paintColour;
    private Bitmap overViewImage, spinnerImage;
    private int sweepAngle = 0;
    private int viewWidth = 0, viewHeight = 0;
    private boolean isIndeterminate = false;
    private int rotation = 0;

    public RefreshView(Context context) {
        super(context);
        init();
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshView, 0, 0);
        paintColour = a.getColor(R.styleable.RefreshView_refreshColour, Color.BLUE);
        int imageID = a.getResourceId(R.styleable.RefreshView_overViewImage,-1);
        int spinnerID = a.getResourceId(R.styleable.RefreshView_spinnerImage, -1);

        if(imageID != -1) {
            overViewImage = BitmapFactory.decodeResource(getResources(),imageID);
        }
        if(spinnerID != -1){
            spinnerImage = BitmapFactory.decodeResource(getResources(),spinnerID);
        }

        a.recycle();
        init();
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshView, 0, 0);
        paintColour = a.getColor(R.styleable.RefreshView_refreshColour, Color.BLUE);
        int imageID = a.getResourceId(R.styleable.RefreshView_overViewImage,-1);
        int spinnerID = a.getResourceId(R.styleable.RefreshView_spinnerImage, -1);

        if(imageID != -1) {
            overViewImage = BitmapFactory.decodeResource(getResources(),imageID);
        }
        if(spinnerID != -1){
            spinnerImage = BitmapFactory.decodeResource(getResources(),spinnerID);
        }

        a.recycle();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshView, 0, 0);
        paintColour = a.getColor(R.styleable.RefreshView_refreshColour, Color.BLUE);
        int imageID = a.getResourceId(R.styleable.RefreshView_overViewImage,-1);
        int spinnerID = a.getResourceId(R.styleable.RefreshView_spinnerImage, -1);

        if(imageID != -1) {
            overViewImage = BitmapFactory.decodeResource(getResources(),imageID);
        }
        if(spinnerID != -1){
            spinnerImage = BitmapFactory.decodeResource(getResources(),spinnerID);
        }

        a.recycle();
        init();
    }

    private void init(){
        mCirclePaint = new Paint();
        mCirclePaint.setColor(paintColour);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mGreyBackgroundPaint = new Paint();
        mGreyBackgroundPaint.setColor(getResources().getColor(R.color.background_light_grey));
        mGreyBackgroundPaint.setStyle(Paint.Style.FILL);

        mWhiteBackgroundPaint = new Paint();
        mWhiteBackgroundPaint.setColor(getResources().getColor(R.color.pure_white));
        mWhiteBackgroundPaint.setStyle(Paint.Style.FILL);

        oval = new RectF();
        oval.set(0, 0, viewWidth, viewHeight);

        rotator = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isIndeterminate){
            rotation -= 5;

            if(rotation < -50){
                rotation = -50;
            }

            canvas.drawRect(0,0,viewWidth, viewHeight, mWhiteBackgroundPaint);
            rotator.postRotate(rotation, viewWidth / 2.0f, viewHeight / 2.0f);
            canvas.drawBitmap(spinnerImage, rotator, null);

        }else{
            rotation = 0;
            rotator.setRotate(rotation, viewWidth / 2.0f, viewHeight / 2.0f);
            canvas.drawRect(0,0,viewWidth, viewHeight, mGreyBackgroundPaint);
            canvas.drawArc(oval, -210, sweepAngle, true, mCirclePaint);

            if(overViewImage != null){
                canvas.drawBitmap(overViewImage,0,0,null);
            }
        }
    }

    public void updateSweepAngle(int newAngle){
        this.sweepAngle = newAngle;
    }

    public void setIndeterminate(boolean indeterminate){
        this.isIndeterminate = indeterminate;
        sweepAngle = 0;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewWidth = xNew;
        viewHeight = yNew;
        oval.set(0, 0, viewWidth, viewHeight);
        overViewImage = Bitmap.createScaledBitmap(overViewImage, viewWidth, viewHeight, true );
        spinnerImage = Bitmap.createScaledBitmap(spinnerImage, viewWidth, viewHeight, true );


    }


}
