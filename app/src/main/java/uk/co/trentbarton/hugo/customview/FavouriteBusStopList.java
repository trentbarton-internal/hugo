package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import uk.co.trentbarton.hugo.activities.StopDetailsActivity;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.interfaces.OnPredictionClickedListener;
import uk.co.trentbarton.hugo.interfaces.OnStopItemStatusChangedListener;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class FavouriteBusStopList extends LinearLayout implements OnStopItemStatusChangedListener {

    ArrayList<Stop> mStopList;
    ArrayList<BusStopItem> mChildItems;
    OnPredictionClickedListener mListener;

    public FavouriteBusStopList(Context context) {
        super(context);
        init();
    }

    public FavouriteBusStopList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FavouriteBusStopList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FavouriteBusStopList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        mStopList = new ArrayList<>();
        mChildItems = new ArrayList<>();
        this.setOrientation(VERTICAL);
        refreshView();
    }

    public void refreshView() {
        getStopsFromDatabase();
        if(mStopList == null || mStopList.size() == 0){
            showEmptyMessage();
        }else{
            addStopsToView();
        }
    }

    public void pauseSearching() {
        for(BusStopItem item : mChildItems){
            item.pauseSearching();
        }
    }

    public void restartSearching() {
        for(BusStopItem item : mChildItems){
            item.resumeSearching();
        }
    }

    private void getStopsFromDatabase(){
        Database db = new Database(getContext());
        mStopList = db.getFavouriteStops();
    }

    private void addStopsToView(){

        this.removeAllViews();
        mChildItems.clear();

        for (Stop stop: mStopList) {
            BusStopItem item = new BusStopItem(getContext(), stop);
            item.setOnPredictionClickedListener(mListener);
            item.setOnStopItemStatusChangedListener(this);
            mChildItems.add(item);

            //Open the first item in the list
            if(mChildItems.size() == 1){
                item.openStop();
            }

            addView(item);
        }
    }

    public void setOnPredictionClickedListner(OnPredictionClickedListener l){
        mListener = l;
    }

    private void showEmptyMessage() {

        this.removeAllViews();

        TextView banner = new TextView(this.getContext());
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,Metrics.densityPixelsToPixels(1));
        banner.setLayoutParams(bannerParams);
        banner.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.background_light_grey));

        TextView tv = new TextView(this.getContext());
        tv.setText(getResources().getString(R.string.live_favourite_card_empty_message));
        int paddingAmount = Metrics.densityPixelsToPixels(25);
        tv.setPadding(paddingAmount,paddingAmount,paddingAmount,paddingAmount);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                1.0f);
        tv.setLayoutParams(params);

        this.addView(banner);
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
