package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class BusTrackingDialog extends RelativeLayout {

    RealtimePrediction mPrediction;
    RelativeLayout mParent;
    Point mBusPosition;
    ImageView arrow;
    TextView mServiceName, mPredictionDisplay;
    TextView freeWifi, usbPower;
    ImageView wifiEnabled, usbEnabled;

    public BusTrackingDialog(Context context, RelativeLayout parent) {
        super(context);
        mParent = parent;
        init();
    }

    public BusTrackingDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BusTrackingDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BusTrackingDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init(){

        this.setVisibility(VISIBLE);
        View view = inflate(getContext(), R.layout.object_realtime_bus_tracking_dialog, this);
        mServiceName = view.findViewById(R.id.object_realtime_bus_tracking_dialog_servicename);
        mPredictionDisplay = view.findViewById(R.id.object_realtime_bus_tracking_dialog_prediction_display);
        freeWifi = view.findViewById(R.id.object_realtime_bus_tracking_dialog_free_wifi_text);
        usbPower = view.findViewById(R.id.object_realtime_bus_tracking_dialog_usb_text);
        wifiEnabled = view.findViewById(R.id.object_realtime_bus_tracking_dialog_wifi_enabled_image);
        usbEnabled = view.findViewById(R.id.object_realtime_bus_tracking_dialog_usb_enabled_image);
        arrow = new ImageView(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(20));
        arrow.setLayoutParams(layoutParams);
        arrow.setVisibility(VISIBLE);
        arrow.setImageDrawable(getResources().getDrawable(R.drawable.bottom_arrow));
        mParent.addView(arrow);
        mParent.addView(this);

    }

    public void showDialog(Point position, RealtimePrediction prediction){

        mPrediction = prediction;
        mBusPosition = position;

        setNewPosition();
        updateInformation();

        this.setVisibility(VISIBLE);
        arrow.setVisibility(VISIBLE);

    }

    public void updateInformation(){

        if(mPrediction == null){
            return;
        }

        if(mPrediction.isHasWifi()){
            wifiEnabled.setVisibility(VISIBLE);
            freeWifi.setVisibility(VISIBLE);
        }else{
            wifiEnabled.setVisibility(GONE);
            freeWifi.setVisibility(GONE);
        }

        if(mPrediction.isHasUsb()){
            usbEnabled.setVisibility(VISIBLE);
            usbPower.setVisibility(VISIBLE);
        }else{
            usbEnabled.setVisibility(GONE);
            usbPower.setVisibility(GONE);
        }

        mPredictionDisplay.setText(mPrediction.getFormattedPredictionDisplay());
        mServiceName.setText(mPrediction.getServiceName());

    }



    public void setNewPosition() {

        if(mBusPosition == null){
            return;
        }

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        if(mBusPosition.y > (screenHeight/2)){
            //busposition is on the bottom half of the screen so show the message on the top
            this.setY(mBusPosition.y - Metrics.densityPixelsToPixels(55) - this.getMeasuredHeight());
            arrow.setRotation(0f);
            arrow.setY(mBusPosition.y - Metrics.densityPixelsToPixels(60));
        }else{
            arrow.setRotation(180.0f);
            arrow.setY(mBusPosition.y - Metrics.densityPixelsToPixels(5));
            this.setY(mBusPosition.y + Metrics.densityPixelsToPixels(10));
        }

        if(mBusPosition.x > (screenWidth / 2)){
            //bus is on right hand side of screen
            this.setX(Math.min(mBusPosition.x - (this.getMeasuredWidth()/2), screenWidth - this.getMeasuredWidth()));
            arrow.setX(Math.min(mBusPosition.x - (arrow.getMeasuredWidth()/2), screenWidth - arrow.getMeasuredWidth() - Metrics.densityPixelsToPixels(5)));
        }else{
            this.setX(Math.max(mBusPosition.x - (this.getMeasuredWidth()/2), 0));
            arrow.setX(Math.max(mBusPosition.x - (arrow.getMeasuredWidth()/2), Metrics.densityPixelsToPixels(5)));

        }
    }

    public void hideDialog(){
        this.setVisibility(GONE);
        arrow.setVisibility(GONE);
    }





}
