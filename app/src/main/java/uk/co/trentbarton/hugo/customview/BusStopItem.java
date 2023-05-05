package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.StopDetailsActivity;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.customviewcontrollers.RefreshViewController;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.interfaces.OnPredictionClickedListener;
import uk.co.trentbarton.hugo.interfaces.OnRefreshDataListener;
import uk.co.trentbarton.hugo.interfaces.OnStopItemStatusChangedListener;
import uk.co.trentbarton.hugo.tools.Metrics;

public class BusStopItem extends LinearLayout implements OnRefreshDataListener {

    private final Stop mStop;
    private View mView;
    private OnStopItemStatusChangedListener mListener;
    private OnPredictionClickedListener mOnPredictionClickedListener;
    private TextView mStopNameText;
    private ImageView mExpandIcon;
    private LinearLayout mPredictionHolder;
    private RefreshView mRefreshIcon;
    private RefreshViewController mRefreshController;
    private boolean isExpanded;

    public BusStopItem(Context context, Stop stop) {
        super(context);
        mStop = stop;
        isExpanded = false;
        generateView();
    }

    private void generateView(){
        mView = inflate(getContext(), R.layout.object_realtime_card_list_item, null);
        mStopNameText = mView.findViewById(R.id.object_realtime_card_list_item_stop_name);
        mExpandIcon = mView.findViewById(R.id.object_realtime_card_list_item_expand_button);
        mRefreshIcon = mView.findViewById(R.id.object_realtime_card_list_item_refresh_view);
        mRefreshController = new RefreshViewController();
        RealTimeFullParams params = new RealTimeFullParams(false, getContext()).addAtcoCode(mStop.getAtcoCode());
        mRefreshController.setDataRequestParams(params);
        mRefreshController.setListener(this);
        mRefreshController.setRefreshView(mRefreshIcon);

        mRefreshIcon.setOnClickListener(v -> {
            mRefreshController.refreshNow();
        });

        mExpandIcon.setOnClickListener(view -> {
            expandStop(true);
        });

        mStopNameText.setOnClickListener(view -> {
            expandStop(true);
        });

        mStopNameText.setText(mStop.getOverrideName());
        this.addView(mView);
    }

    public void setOnStopItemStatusChangedListener(OnStopItemStatusChangedListener l) {
        mListener = l;
    }

    public void setOnPredictionClickedListener(OnPredictionClickedListener l) {
        mOnPredictionClickedListener = l;
    }

    public void openStop(){
        expandStop(false);
    }

    public void closeStop(){

        isExpanded = false;
        mRefreshIcon.setVisibility(View.GONE);
        mExpandIcon.animate().rotation(0.0f).setDuration(200).start();
        mRefreshController.stopRefreshing();
        if(mPredictionHolder != null){
            mPredictionHolder.removeAllViews();
        }

    }

    private void expandStop(boolean callListener){
        if(isExpanded) {
            //Simply open the stop and go into the new Activity
            Intent intent = new Intent(getContext(), StopDetailsActivity.class);
            intent.putExtra("stop", mStop);
            getContext().startActivity(intent);
        }else{
            //Expand the stop and start searching for the results
            mExpandIcon.animate().rotation(-90.0f).setDuration(200).start();

            //Make a callback to the holder View to close all the other views
            if(mListener != null && callListener) {
                mListener.onStopOpened(this);
            }

            mRefreshIcon.setVisibility(View.VISIBLE);
            isExpanded = true;
            startTask();

        }
    }

    private void startTask(){

        mRefreshController.startRefreshing();
        mRefreshController.refreshNow();

    }

    public void pauseSearching(){
        if(isExpanded){
            mRefreshController.stopRefreshing();
        }
    }

    public void resumeSearching(){
        if(isExpanded){
            mRefreshController.startRefreshing();
            mRefreshController.refreshNow();
        }
    }

    private void showPredictions(){

        if(!isExpanded){
            return;
        }

        mPredictionHolder = mView.findViewById(R.id.object_realtime_card_list_item_content_holder);
        mPredictionHolder.removeAllViews();
        mStop.resetFilter(getContext());

        if(mStop.getFilteredPredictions(getContext()).size() == 0){
            TextView tv = new TextView(getContext());
            tv.setText(getResources().getString(R.string.live_no_realtime_for_this_stop));
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(Metrics.densityPixelsToPixels(15),Metrics.densityPixelsToPixels(15),Metrics.densityPixelsToPixels(15),Metrics.densityPixelsToPixels(15));
            tv.setTextColor(Color.BLACK);
            mPredictionHolder.addView(tv);
        }


        for(int x = 0; x < mStop.getFilteredPredictions(getContext()).size(); x++){

            RealtimePrediction prediction = mStop.getFilteredPredictions(getContext()).get(x);
            View predictionView = inflate(getContext(), R.layout.object_realtime_prediction_item, null);
            predictionView.setOnClickListener(v -> {
                if(mOnPredictionClickedListener != null){

                    ArrayList<Stop> stops = new ArrayList<>();
                    stops.add(mStop);
                    MapHelper.getInstance().setSelectedStops(stops);
                    mOnPredictionClickedListener.OnPredictionClicked(prediction);
                }
            });
            ImageView circle = predictionView.findViewById(R.id.object_realtime_prediction_service_colour);
            circle.setBackgroundColor(prediction.getServiceColour());
            TextView serviceName = predictionView.findViewById(R.id.object_realtime_prediction_service_name);
            serviceName.setText(prediction.getServiceName());
            TextView serviceDestination = predictionView.findViewById(R.id.object_realtime_prediction_destination_name);
            serviceDestination.setText("to " + prediction.getJourneyDestination());
            TextView predictionTime = predictionView.findViewById(R.id.object_realtime_prediction_time);
            predictionTime.setText(prediction.getFormattedPredictionDisplay());

            if(prediction.isCancelledService()){
                serviceDestination.setText("cancelled");
                serviceDestination.setTextColor(Color.parseColor("#FF0000"));
                predictionTime.setTextColor(Color.parseColor("#FF0000"));
            }else{
                serviceDestination.setText("to " + prediction.getJourneyDestination());
                serviceDestination.setTextColor(Color.parseColor("#000000"));
                predictionTime.setTextColor(Color.parseColor("#000000"));
            }

            mPredictionHolder.addView(predictionView);

            if(x == mStop.getFilteredPredictions(getContext()).size() - 1 || x == 2){
                TextView bottomLine = predictionView.findViewById(R.id.object_realtime_bottom_line);
                bottomLine.setVisibility(GONE);
                break;
            }
        }

        if(mStop.stopHasActiveFilter(getContext())){
            //Add the filters applied icon to the bottom
            inflate(getContext(), R.layout.object_realtime_prediction_filters_applied_icon, mPredictionHolder);
        }
    }


    @Override
    public void onDataRefreshed(Object o) {

        mStop.getPredictions().clear();

        if (o instanceof ArrayList<?>) {
            for (int i = 0; i < ((ArrayList) o).size(); i++) {
                if (((ArrayList) o).get(i) instanceof RealtimePrediction) {
                    mStop.getPredictions().add((RealtimePrediction) ((ArrayList) o).get(i));
                }
            }
        }

        //Run showing results on UI thread
        Handler mainHandler = new Handler(getContext().getMainLooper());
        Runnable myRunnable = this::showPredictions;
        mainHandler.post(myRunnable);

    }
}
