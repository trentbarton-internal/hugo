package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import java.util.Date;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.interfaces.RealtimeTrackerUpdateListener;
import uk.co.trentbarton.hugo.tasks.RealtimeTracker;

public class RealtimeJourneyStep extends ScheduledJourneyStep implements RealtimeTrackerUpdateListener {

    private String atcoCode,predictionDisplay;
    private TextView mMinsText, mMinsAmount;
    private int predictionInSeconds;
    private long vehicleNumber;
    private LatLng vehiclePosition;
    private LocalDateTime scheduledDepartureTime, actualDepartureTime;
    private RealtimeTracker mTracker;

    public RealtimeJourneyStep(JSONObject object) throws Exception{
        super(object);
        JSONObject pred = object.getJSONObject("realtime_prediction");
        this.atcoCode = pred.getString("stop_ref");
        this.predictionDisplay = pred.getString("prediction_display");
        this.predictionInSeconds = pred.getInt("prediction_in_seconds");
        this.vehicleNumber = pred.getLong("vehicle_number");
        this.vehiclePosition = new LatLng(pred.getDouble("vehicle_location_lat"), pred.getDouble("vehicle_location_lng"));
        this.scheduledDepartureTime = LocalDateTime.fromDateFields(new Date(pred.getLong("scheduled_departure_time") * 1000L));
        this.actualDepartureTime = LocalDateTime.fromDateFields(new Date(pred.getLong("actual_departure_time") * 1000L));
    }

    @Override
    public View getView(Context context) {

        mContext = context;
        if(mView == null){
            mView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_realtime, null);
            init(context);
        }

        return mView;
    }

    private void init(Context context) {

        ImageView busColourRing = mView.findViewById(R.id.busColourRing);
        TextView serviceName = mView.findViewById(R.id.service_name_text);
        ImageView realtimeCircle = mView.findViewById(R.id.realtime_circle);
        TextView destinationText = mView.findViewById(R.id.destination_text);
        LinearLayout mangoHolder = mView.findViewById(R.id.mangoHolder);
        LinearLayout wifiIndication = mView.findViewById(R.id.free_wifi_indicator);
        LinearLayout usbIndicator = mView.findViewById(R.id.usb_power_indicator);
        TextView sitBackText = mView.findViewById(R.id.sit_back_text);

        mMinsText = mView.findViewById(R.id.minsText);
        mMinsAmount = mView.findViewById(R.id.minsAmount);

        busColourRing.setBackgroundColor(this.getServiceColour());
        serviceName.setText(this.getServiceName());
        destinationText.setText("Towards " + this.getServiceDestination());

        if(this.isBusHasMango()){
            mangoHolder.setVisibility(View.VISIBLE);
        }else{
            mangoHolder.setVisibility(View.GONE);
        }

        if(this.isBusHasUsb()){
            usbIndicator.setVisibility(View.VISIBLE);
        }else{
            usbIndicator.setVisibility(View.GONE);
        }

        if(this.isBusHasWifi()){
            wifiIndication.setVisibility(View.VISIBLE);
        }else{
            wifiIndication.setVisibility(View.GONE);
        }

        sitBackText.setText("sit back and relax for around " + this.getDurationText());

        Animation heartbeatAnimation = AnimationUtils.loadAnimation(context, R.anim.heartbeat_animation);
        realtimeCircle.startAnimation(heartbeatAnimation);

        if(this.getScheduledDepartureTime().isBefore(LocalDateTime.now())){
            mMinsText.setText("-");
            mMinsAmount.setVisibility(View.GONE);
        }else{
            if(this.getPredictionDisplay().equalsIgnoreCase("due")){
                mMinsText.setText("due");
                mMinsAmount.setVisibility(View.GONE);
            }else{
                String[] amounts = this.getPredictionDisplay().split(" ");
                mMinsAmount.setVisibility(View.VISIBLE);
                mMinsText.setText(amounts[0]);
                mMinsAmount.setText(amounts[1]);
            }
        }

        RelativeLayout holder = mView.findViewById(R.id.holder);
        holder.setOnClickListener(v -> {
            this.itemClicked();
        });

        mTracker = new RealtimeTracker(getServiceName(), getVehicleNumber(), getAtcoCode());
        mTracker.setRealtimeTrackerUpdateListener(this);
        mTracker.startRefreshing(mContext);

    }

    public long getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(long vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getAtcoCode() {
        return atcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        this.atcoCode = atcoCode;
    }

    public String getPredictionDisplay() {
        return predictionDisplay;
    }

    public void setPredictionDisplay(String predictionDisplay) {
        this.predictionDisplay = predictionDisplay;
    }

    public int getPredictionInSeconds() {
        return predictionInSeconds;
    }

    public void setPredictionInSeconds(int predictionInSeconds) {
        this.predictionInSeconds = predictionInSeconds;
    }

    public LatLng getVehiclePosition() {
        return vehiclePosition;
    }

    public void setVehiclePosition(LatLng vehiclePosition) {
        this.vehiclePosition = vehiclePosition;
    }

    public LocalDateTime getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(LocalDateTime scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public LocalDateTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(LocalDateTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    @Override
    public void onPredictionUpdated(RealtimePrediction prediction) {

        String predictionText = prediction.getPredictionDisplay();

        if(predictionText.trim().equalsIgnoreCase("due")){
            mMinsText.setText("due");
            mMinsAmount.setVisibility(View.GONE);
        }else{
            String[] amounts = predictionText.split(" ");
            if(amounts.length == 1){
                mMinsText.setText(amounts[0]);
                mMinsAmount.setVisibility(View.GONE);
            }else{
                mMinsText.setText(amounts[0].trim());
                mMinsAmount.setText(amounts[1].trim());
            }
        }
    }

    @Override
    public void onErrorReceived() {
        mMinsText.setText("-");
        mMinsAmount.setVisibility(View.GONE);
    }
}
