package uk.co.trentbarton.hugo.fragments.journeychosenmapitems;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.LocalDateTime;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.RealtimeJourneyStep;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.interfaces.RealtimeTrackerUpdateListener;
import uk.co.trentbarton.hugo.tasks.RealtimeTracker;
import uk.co.trentbarton.hugo.tools.Metrics;

public class RealtimeJourneyFragment extends Fragment implements RealtimeTrackerUpdateListener {

    private TextView mServiceName, mServiceDestination, sitBackAndRelaxText, featuresDivider, errorMessage, mMinsText, mMinsUnitText;
    private ImageView mServiceColour, blueCircle;
    private int mJourneyIndex, mStepIndex;
    private LinearLayout mMangoHolder, mFreeWifiHolder, mUsbPowerHolder, featuresHolder;
    private RealtimeJourneyStep mJourneyStep;
    private RealtimeTracker mTracker;

    public static RealtimeJourneyFragment newInstance(int journeyIndex, int stepIndex){
        RealtimeJourneyFragment fragment = new RealtimeJourneyFragment();
        Bundle args = new Bundle();
        args.putInt("journeyIndex", journeyIndex);
        args.putInt("stepIndex", stepIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public void readBundle(Bundle bundle) {
        if (bundle != null) {
            mJourneyIndex = bundle.getInt("journeyIndex",0);
            mStepIndex = bundle.getInt("stepIndex", 0);
        }
        try{
            mJourneyStep = (RealtimeJourneyStep) GlobalData.getInstance().getJourneyData().get(mJourneyIndex).getSteps().get(mStepIndex);
        }catch(Exception ignore){}

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            readBundle(getArguments());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        readBundle(savedInstanceState);
        View view = inflater.inflate(R.layout.journey_map_item_realtime_fragment, container, false);
        mServiceName = view.findViewById(R.id.journey_map_item_service_name);
        mServiceDestination = view.findViewById(R.id.journey_map_item_destination);
        sitBackAndRelaxText = view.findViewById(R.id.journey_map_item_sit_back_text);
        mServiceColour = view.findViewById(R.id.journey_map_item_service_colour);
        mMangoHolder = view.findViewById(R.id.journey_map_item_mango_holder);
        mFreeWifiHolder = view.findViewById(R.id.journey_map_item_free_wifi_holder);
        mUsbPowerHolder = view.findViewById(R.id.journey_map_item_usb_power_holder);
        featuresDivider = view.findViewById(R.id.journey_map_item_features_divider);
        featuresHolder = view.findViewById(R.id.journey_map_item_features_holder);
        blueCircle = view.findViewById(R.id.journey_map_item_realtime_circle);
        errorMessage = view.findViewById(R.id.journey_map_item_realtime_error_message);
        mMinsText = view.findViewById(R.id.journey_map_item_realtime_mins_counter);
        mMinsUnitText = view.findViewById(R.id.journey_map_item_realtime_mins_text);
        if(mJourneyStep != null) init();
        return view;
    }

    private void init(){

        mServiceName.setText(mJourneyStep.getServiceName());
        mServiceDestination.setText(String.format("towards %s",mJourneyStep.getServiceDestination()));
        sitBackAndRelaxText.setText(String.format("SIT BACK AND RELAX FOR AROUND %s",mJourneyStep.getDurationText()));
        mServiceColour.setBackgroundColor(mJourneyStep.getServiceColour());
        if(mJourneyStep.isBusHasMango()){
            mMangoHolder.setVisibility(View.VISIBLE);
        }else{
            mMangoHolder.setVisibility(View.GONE);
        }

        int paddingValue = Metrics.densityPixelsToPixels(10);

        if(mJourneyStep.isBusHasUsb() || mJourneyStep.isBusHasWifi()){
            featuresHolder.setVisibility(View.VISIBLE);
            featuresDivider.setVisibility(View.VISIBLE);
            mUsbPowerHolder.setVisibility(View.GONE);
            mFreeWifiHolder.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) sitBackAndRelaxText.getLayoutParams()).weight = 190;
            sitBackAndRelaxText.setGravity(Gravity.CENTER_VERTICAL);
            sitBackAndRelaxText.setPadding(paddingValue,0,paddingValue,0);

            if(mJourneyStep.isBusHasUsb()){
                mUsbPowerHolder.setVisibility(View.VISIBLE);
            }

            if(mJourneyStep.isBusHasWifi()){
                mFreeWifiHolder.setVisibility(View.VISIBLE);
            }

        }else{
            featuresHolder.setVisibility(View.GONE);
            featuresDivider.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) sitBackAndRelaxText.getLayoutParams()).weight = 301;
            sitBackAndRelaxText.setPadding(paddingValue,paddingValue,paddingValue,paddingValue);
            sitBackAndRelaxText.setGravity(Gravity.CENTER);
        }

        Animation heartbeatAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.heartbeat_animation);
        blueCircle.startAnimation(heartbeatAnimation);
        mTracker = new RealtimeTracker(mJourneyStep.getServiceName(), mJourneyStep.getVehicleNumber(), mJourneyStep.getAtcoCode());
        mTracker.setRealtimeTrackerUpdateListener(this);
        mTracker.startRefreshing(getContext());
        updateRealtimePrediction();

    }

    public void updateRealtimePrediction(){
        //Get the value from the journey
        LocalDateTime startTime = mJourneyStep.getActualDepartureTime();

        int seconds = (int)((startTime.toDate().getTime() - LocalDateTime.now().toDate().getTime()) / 1000);

        if(startTime.isBefore(LocalDateTime.now().minusMinutes(1))){
            mMinsText.setText("-");
            mMinsUnitText.setVisibility(View.GONE);
            return;
        }

        if(seconds < 60){
            mMinsUnitText.setVisibility(View.GONE);
            mMinsText.setText("due");
        }else if(seconds < 90){
            mMinsUnitText.setVisibility(View.VISIBLE);
            mMinsUnitText.setText("min");
            mMinsText.setText("1");
        }else{
            int minutes = seconds / 60;
            mMinsUnitText.setVisibility(View.VISIBLE);
            mMinsUnitText.setText("mins");
            mMinsText.setText(String.valueOf(minutes));
        }
    }

    public void updateRealtimePrediction(RealtimePrediction prediction){

        String predictionText = prediction.getPredictionDisplay();

        if(predictionText.equalsIgnoreCase("due")){
            mMinsUnitText.setVisibility(View.GONE);
            mMinsText.setText("due");
        }else{
            String[] parts = predictionText.split(" ");
            if(parts.length == 2){
                mMinsUnitText.setVisibility(View.VISIBLE);
                mMinsUnitText.setText(parts[1]);
                mMinsText.setText(parts[0]);
            }else{
                mMinsUnitText.setVisibility(View.GONE);
                mMinsText.setText(parts[0]);
            }
        }
    }

    public void showErrorContactingServer(){
        errorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPredictionUpdated(RealtimePrediction prediction) {
        updateRealtimePrediction(prediction);
    }

    @Override
    public void onErrorReceived() {
        showErrorContactingServer();
    }
}
