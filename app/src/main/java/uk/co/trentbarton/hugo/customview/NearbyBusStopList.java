package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.Nullable;

import android.location.Location;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import uk.co.trentbarton.hugo.activities.StopDetailsActivity;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetStopsParams;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.interfaces.OnPredictionClickedListener;
import uk.co.trentbarton.hugo.interfaces.OnStopItemStatusChangedListener;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class NearbyBusStopList extends LinearLayout implements OnStopItemStatusChangedListener {

    ArrayList<Stop> mStopList;
    ArrayList<BusStopItem> mChildItems;
    OnPredictionClickedListener mListener;
    private boolean mIsParamsSet = false;
    private Location mCurrentLocation = null;
    private boolean isLocationAvailable = true;
    private boolean isSearching = false;

    public NearbyBusStopList(Context context) {
        super(context);
        init();
    }

    public NearbyBusStopList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NearbyBusStopList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStopList = new ArrayList<>();
        mChildItems = new ArrayList<>();
        this.setOrientation(VERTICAL);
        startTask();
    }

    public boolean isParamsSet(){
        return this.mIsParamsSet;
    }

    public void setLocationAvailable(boolean isAvailable){
        this.isLocationAvailable = isAvailable;
    }

    private void startTask() {

        /*
         *
         * if currentLocation is set
         * Start the progress spinner showing
         * go to the API and request all stops within x distance of current location
         * get results and parse stops that are close to a maximum of 5 closest
         *
         * */

        if (this.mCurrentLocation == null) {
            return;
        }

        if (isSearching) {
            return;
        }

        isSearching = true;

        GetStopsParams params = new GetStopsParams(getContext());
        params.addLocation(mCurrentLocation);
        params.setVersion(0);
        DataRequestTask task = new DataRequestTask(params);
        task.setOnTaskCompletedListener(worked -> {

            isSearching = false;

            if(worked){
                try {
                    ArrayList<Stop> stops = (ArrayList<Stop>) task.getResponse();
                    mStopList = stops;
                } catch (Exception e) {
                }
            }

            refreshView();

        });
        task.execute(getContext());

    }

    public void updateLocation(Location l){
        mCurrentLocation = l;
        startTask();
    }

    public void pauseSearching(){
        for(BusStopItem item : mChildItems){
            item.pauseSearching();
        }
    }

    public void restartSearching(){
        for(BusStopItem item : mChildItems){
            item.resumeSearching();
        }
    }

    public void refreshView() {

        if(isLocationAvailable){
            //Not allowed to access GPS so show gos unavailable
            showNoGps();
        }

        if(mStopList == null || mStopList.size() == 0){
            if(isSearching){
                showStillFindingCurrentLocation();
            }else{
                showEmptyMessage();
            }
        }else{
            addStopsToView();
        }
    }

    private void addStopsToView(){

        this.removeAllViews();
        mChildItems.clear();

        int i = 0;

        for (Stop stop: mStopList) {

            i++;

            if(i > 5){
                break;
            }

            BusStopItem item = new BusStopItem(getContext(), stop);
            item.setOnPredictionClickedListener(mListener);
            item.setOnStopItemStatusChangedListener(this);
            mChildItems.add(item);
            addView(item);
        }
    }

    private void showNoGps(){

        this.removeAllViews();
        TextView tv = new TextView(this.getContext());
        tv.setText(getResources().getString(R.string.live_nearby_card_no_gps_message));
        int paddingAmount = Metrics.densityPixelsToPixels(25);
        tv.setPadding(paddingAmount,paddingAmount,paddingAmount,paddingAmount);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                1.0f);
        tv.setLayoutParams(params);
        this.addView(tv);

    }

    private void showStillFindingCurrentLocation(){

        this.removeAllViews();
        TextView tv = new TextView(this.getContext());
        tv.setText(getResources().getString(R.string.live_nearby_card_not_found_location_message));
        int paddingAmount = Metrics.densityPixelsToPixels(25);
        tv.setPadding(paddingAmount,paddingAmount,paddingAmount,paddingAmount);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                1.0f);
        tv.setLayoutParams(params);
        this.addView(tv);

    }

    public void setOnPredictionClickedListner(OnPredictionClickedListener l){
        mListener = l;
    }

    private void showEmptyMessage() {

        this.removeAllViews();
        TextView tv = new TextView(this.getContext());
        tv.setText(getResources().getString(R.string.live_nearby_card_empty_message));
        int paddingAmount = Metrics.densityPixelsToPixels(25);
        tv.setPadding(paddingAmount,paddingAmount,paddingAmount,paddingAmount);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                1.0f);
        tv.setLayoutParams(params);
        this.addView(tv);
    }


    @Override
    public void onStopClosed(BusStopItem stop) {

    }

    @Override
    public void onStopOpened(BusStopItem stop) {
        closeAllOtherStops(stop);
    }

    private void closeAllOtherStops(BusStopItem stop) {
        for(BusStopItem item: mChildItems){
            if(item != stop){
                item.closeStop();
            }
        }
    }
}
