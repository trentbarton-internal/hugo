package uk.co.trentbarton.hugo.activities;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.customadapters.StopDetailsAdapter;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpImageItem;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenu;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenuItem;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpServiceItem;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpTextItem;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.dataholders.Alarm;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.CreateAlarmParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DeleteAlarmParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.SendPushTokenParams;
import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.ServiceFilter;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomEditTextDialog;
import uk.co.trentbarton.hugo.dialogs.CustomImageDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tools.Tools;
import uk.co.trentbarton.hugo.widget.RealtimeWidgetProvider;

public class StopDetailsActivity extends AppCompatActivity implements OnTaskCompletedListener {

    private Stop mStop;
    private TextView alarmCancelButton, alarmAcceptButton;
    private ListView mStopList;
    private SwipeRefreshLayout mSwipeLayout;
    private RelativeLayout mNumberPickerHolder, mLoadingScreen, mAlarmSetScreen, mAlarmRemovedScreen;
    private ServiceFilter mFilter;
    private LinearLayout alarmsButton, filterButton, planButton, favouriteButton, bottomNavBar, bottomAcceptCancelBar;
    private NumberPicker mNumberPicker;
    private SlideUpMenu slideUpMenu;
    private StopDetailsAdapter mAdapter;
    private Database mDatabase;
    ArrayList<SlideUpMenuItem> menuItems;
    ArrayList<RealtimePrediction> selectedPredictions;
    DataRequestTask mTask;
    Handler mHandler;
    Runnable mRunnable;
    private boolean IS_NORMAL_VIEW = true;
    private String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);
        menuItems = new ArrayList<>();

        mDatabase = new Database(this);
        selectedPredictions = new ArrayList<>();

        showNormalActionBar();
        init();
        startRefreshing();

    }

    public void showCustomActionBar(String question){

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_custom_layout);
        actionBar.setDisplayHomeAsUpEnabled(false);
        ((TextView) findViewById(R.id.action_bar_title)).setText(question);
    }

    public void showNormalActionBar(){

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){

            if(mStop == null){
                try{
                    mStop = getIntent().getExtras().getParcelable("stop");
                }catch(Exception ignore){}
            }

            if(mStop != null) actionBar.setTitle(mStop.getOverrideName());
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setCustomView(null);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    private void init() {
        mStop.applyFilterFromPreferences(this);
        mStopList = findViewById(R.id.activity_stop_details_list);
        mAdapter = new StopDetailsAdapter(this,mStop.getFilteredPredictions(this));
        mSwipeLayout = findViewById(R.id.swipeLayout);
        mStopList.setAdapter(mAdapter);
        alarmsButton = findViewById(R.id.stop_details_alarm_button);
        filterButton = findViewById(R.id.stop_details_filter_button);
        planButton = findViewById(R.id.stop_details_plan_button);
        favouriteButton = findViewById(R.id.stop_details_favourite_button);
        slideUpMenu = findViewById(R.id.activity_stop_details_slide_up_menu);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        bottomAcceptCancelBar = findViewById(R.id.bottomAcceptCancelBar);
        alarmCancelButton = findViewById(R.id.bottomAcceptCancelBarCancelButton);
        alarmAcceptButton = findViewById(R.id.bottomAcceptCancelBarAcceptButton);
        mNumberPicker = findViewById(R.id.minutePicker);
        mNumberPickerHolder = findViewById(R.id.minutePickerHolder);
        mLoadingScreen = findViewById(R.id.stop_details_loading_screen);
        mAlarmSetScreen = findViewById(R.id.stop_details_alarm_set_screen);
        mAlarmRemovedScreen = findViewById(R.id.stop_details_alarm_removed_screen);

        mStopList.setOnItemClickListener((parent, view, position, id) -> {

            if(IS_NORMAL_VIEW){

                ArrayList<RealtimePrediction> predictions = new ArrayList<>();
                predictions.add(mStop.getFilteredPredictions(StopDetailsActivity.this).get(position));
                MapHelper.getInstance().setMonitoredVehicles(predictions);
                ArrayList<Stop> stops = new ArrayList<>();
                stops.add(mStop);
                MapHelper.getInstance().setLastStopClicked(mStop);
                MapHelper.getInstance().setSelectedStops(stops);
                onBackPressed();
                this.finish();
            }else{
                mAdapter.setSelectedPosition(position);
                mAdapter.notifyDataSetChanged();
            }

        });

        if(mStop.stopHasActiveFilter(this)){
            showFiltersApplied();
        }else{
            showNoFiltersApplied();
        }

        if(mDatabase.isStopFavourite(mStop.getAtcoCode())){
            showAlreadyFavourite();
        }else{
            showMakeFavourite();
        }

        mSwipeLayout.setOnRefreshListener(() -> {
            mHandler.removeCallbacks(mRunnable);
            startRefreshing();
        });

        planButton.setOnClickListener(v -> {

            menuItems.clear();

            SlideUpImageItem planFrom = new SlideUpImageItem(StopDetailsActivity.this, "Plan a journey from this stop", R.drawable.marker_green);
            planFrom.setOnClickListener(v13 -> {
                GlobalData.getInstance().setNavigateToFragmentPosition(1);
                TomTomPlace place = new TomTomPlace(mStop.getPosition().latitude, mStop.getPosition().longitude, mStop.getOverrideName(), "");
                JourneyParams.getInstance().setFromPlace(place);
                onBackPressed();
            });

            SlideUpImageItem planTo = new SlideUpImageItem(StopDetailsActivity.this, "Plan a journey to this stop", R.drawable.marker_red);
            planTo.setOnClickListener(v14 -> {
                GlobalData.getInstance().setNavigateToFragmentPosition(1);
                TomTomPlace place = new TomTomPlace(mStop.getPosition().latitude, mStop.getPosition().longitude, mStop.getOverrideName(), "");
                JourneyParams.getInstance().setToPlace(place);
                onBackPressed();
            });

            menuItems.add(planFrom);
            menuItems.add(planTo);
            slideUpMenu.assignItems(menuItems);
            slideUpMenu.overrideCancelText("Close");
            slideUpMenu.setOnCloseListener(null);
            slideUpMenu.slideUp();
        });

        favouriteButton.setOnClickListener(v -> {

            if(mDatabase.isStopFavourite(mStop)) {
                showDeleteAsFavouriteDialog();
            }else{
                showSaveAsFavouriteDialog();
            }
        });

        filterButton.setOnClickListener(v -> {
            showFiltersMenu();
        });

        alarmsButton.setOnClickListener(v -> {
            setAlarms();
        });

        alarmCancelButton.setOnClickListener( v -> {
            bottomNavBar.setVisibility(View.VISIBLE);
            bottomAcceptCancelBar.setVisibility(View.GONE);
            mNumberPickerHolder.setVisibility(View.GONE);
            animateAdapter(200);
            showNormalActionBar();
        });

        alarmAcceptButton.setOnClickListener(v -> {
            alarmsProceed();
        });

        mLoadingScreen.setOnClickListener(v -> {
            //Do nothing
        });

        mAlarmSetScreen.setOnClickListener(v -> {
            //Do nothing
        });

        refreshAlarmIcon();

    }

    private void alarmsProceed() {

        if(alarmAcceptButton.getText().toString().equalsIgnoreCase("Set time")){
            //We have a bus we want to set an alarm for so validate and then show the time away spinner
            if(validateSelectedVehicle()){
                showNumberPicker();
            }
        }else{
            //We have a time that the user has chosen so validate and send Alarm request to served
            Alarm alarm = new Alarm();
            alarm.setAtcoCode(mStop.getAtcoCode());
            alarm.setServiceName(mAdapter.getSelectedItem().getServiceName());
            alarm.setScheduledTime(mAdapter.getSelectedItem().getScheduledDepartureTime());
            alarm.setMinuteTrigger(mNumberPicker.getValue());
            alarm.setStopName(mStop.getOverrideName());
            sendAlarmRequest(alarm);
        }
    }

    private void checkPushSent(Alarm alarm){

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();

                Log.d(TAG, "Server token:" + newToken);
                HugoPreferences.setPushToken(StopDetailsActivity.this,newToken);
                HugoPreferences.setPushSent(StopDetailsActivity.this, false);
                SendPushTokenParams params = new SendPushTokenParams(StopDetailsActivity.this);
                params.setPushToken(newToken);
                DataRequestTask task = new DataRequestTask(params);
                task.setOnTaskCompletedListener(result -> {
                    if(result){
                        Log.d(TAG, "Token successfully sent to server");
                        HugoPreferences.setPushSent(StopDetailsActivity.this, true);
                    }else{
                        Log.d(TAG, "Token update to hugo servers failed because " + task.getErrorMessage());
                    }
                    sendAlarmRequest(alarm);
                });
                task.execute(StopDetailsActivity.this);
            }
        });

    }



    private void sendAlarmRequest(Alarm alarm) {

        showLoading();

        boolean isPushSent = HugoPreferences.isPushSent(this);
        String token = HugoPreferences.getPushToken(this);

        if((!HugoPreferences.isPushSent(this)) || HugoPreferences.getPushToken(this).isEmpty()){
            checkPushSent(alarm);
            return;
        }

        CreateAlarmParams mParms = new CreateAlarmParams(this);
        mParms.addAlarm(alarm);
        DataRequestTask task = new DataRequestTask(mParms);
        task.setOnTaskCompletedListener(result -> {
            if(result){
                alarm.setAlarmID((Integer)task.getResponse());
                HugoPreferences.setActiveAlarm(StopDetailsActivity.this, alarm);
                showAlarmActiveIcon();
                hideLoading();
                showAlarmSet();
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    hideAlarmSet();
                    bottomNavBar.setVisibility(View.VISIBLE);
                    bottomAcceptCancelBar.setVisibility(View.GONE);
                    mNumberPickerHolder.setVisibility(View.GONE);
                    animateAdapter(200);
                    showNormalActionBar();
                }, 1500);
            }else{
                Toast.makeText(this, "Failed to register alarm with server", Toast.LENGTH_LONG).show();
            }
        });

        task.execute(this);
    }

    private void showCancelAlarmMenu(){

        Alarm alarm = HugoPreferences.getActiveAlarm(this);
        menuItems.clear();

        SlideUpTextItem item = new SlideUpTextItem(this, "Remove alarm", Color.RED,
                String.format(Locale.ENGLISH,"You can only set one alarm, and you already have an alarm set for: %s due at %s", alarm.getServiceName(), alarm.getScheduledTime().toString("HH:mm")));

        item.setOnClickListener(v -> {
            showLoading();
            DeleteAlarmParams mParams = new DeleteAlarmParams(this);
            mParams.addAlarm(HugoPreferences.getActiveAlarm(this));
            DataRequestTask task = new DataRequestTask(mParams);
            task.setOnTaskCompletedListener(successful -> {
                hideLoading();
                if(successful){
                    HugoPreferences.setActiveAlarm(this, null);
                    showAlarmInactiveIcon();
                    showAlarmUnSet();
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        hideAlarmUnSet();
                    }, 1500);
                }else{
                    Toast.makeText(this, "Failed to remove alarm from the server", Toast.LENGTH_SHORT).show();
                }
            });
            task.execute(this);
            slideUpMenu.slideDown();

        });

        menuItems.add(item);
        slideUpMenu.assignItems(menuItems);
        slideUpMenu.overrideCancelText("Cancel");
        slideUpMenu.setOnCloseListener(null);
        slideUpMenu.slideUp();
    }

    private void showLoading(){
        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    private void hideLoading(){
        mLoadingScreen.setVisibility(View.GONE);
    }

    private void showAlarmSet(){
        mAlarmSetScreen.setVisibility(View.VISIBLE);
    }

    private void hideAlarmSet(){
        mAlarmSetScreen.setVisibility(View.GONE);
    }

    private void showAlarmUnSet(){
        mAlarmRemovedScreen.setVisibility(View.VISIBLE);
    }

    private void hideAlarmUnSet(){
        mAlarmRemovedScreen.setVisibility(View.GONE);
    }

    private void showAlarmActiveIcon(){
        ((ImageView)findViewById(R.id.stop_details_alarm_icon)).setImageResource(R.drawable.alarm_green);
    }

    private void showAlarmInactiveIcon(){
        ((ImageView)findViewById(R.id.stop_details_alarm_icon)).setImageResource(R.drawable.alarm_grey);
    }

    private boolean validateSelectedVehicle() {

        RealtimePrediction selectedPrediction = mAdapter.getSelectedItem();

        if(selectedPrediction == null){
            Toast.makeText(this, "Oops... something hasn't worked right please try to select again.", Toast.LENGTH_LONG).show();
            return false;
        }

        if(!selectedPrediction.isWorking()){
            Toast.makeText(this, "Sorry the realtime isn't working on this vehicle, predictions that show clock face times are timetabled departures", Toast.LENGTH_LONG).show();
            return false;
        }

        if(selectedPrediction.getPredictionInSeconds() < 5 * 60){
            Toast.makeText(this, "You can't set an alarm for a service due within 5 minutes", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void showNumberPicker(){

        mNumberPickerHolder.setVisibility(View.VISIBLE);
        mNumberPicker.setMinValue(5);
        mNumberPicker.setMaxValue((int) (Math.floor(mAdapter.getSelectedItem().getPredictionInSeconds() / 60.0) - 1));
        showCustomActionBar("tell me when the bus is...");
        alarmAcceptButton.setText("Set alarm");

    }

    private void setAlarms() {
        //Hide bottom banner show Cancel/Set time
        if(HugoPreferences.getActiveAlarm(this) != null){
            showCancelAlarmMenu();
            return;
        }

        if(Tools.areNotificationsBlocked(this)){
            CustomYesNoDialog dialog = new CustomYesNoDialog(this);
            dialog.setTitle("Notifications are blocked");
            dialog.setContentText("For this feature to work notifications must be enabled, do you want to change these settings?");
            dialog.setAcceptButtonListener(() -> {
                Tools.openNotificationSettings(StopDetailsActivity.this);
                return true;
            });
            dialog.show();
            return;
        }

        bottomNavBar.setVisibility(View.GONE);
        bottomAcceptCancelBar.setVisibility(View.VISIBLE);
        alarmAcceptButton.setText("Set time");
        //Show items in list with radio buttons
        animateAdapter(200);
        //Change the toolbar to ask the question
        showCustomActionBar("Select which bus you want to set an alarm for ...");
    }

    private void animateAllListClosed(){

        AnimatorSet as = new AnimatorSet();
        int childCount = mStopList.getChildCount();
        ArrayList<Animator> list = new ArrayList<Animator>();

        for (int i = 0; i<childCount; ++i) {
            ImageView imv = (ImageView) mStopList.getChildAt(i).findViewById(R.id.object_realtime_prediction_selection_image);
            TextView tv = (TextView) mStopList.getChildAt(i).findViewById(R.id.object_realtime_prediction_time);

            mAdapter.setSelectionMode(false);
            list.add(ObjectAnimator.ofFloat(imv, "x", imv.getRight(), imv.getRight() + imv.getWidth()));
            list.add(ObjectAnimator.ofFloat(tv, "x", tv.getLeft(), tv.getLeft() + imv.getWidth()));

        }

        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        as.playTogether(list);
        as.setDuration(200);
        as.start();
    }

    private void animateAdapter(int duration){

        AnimatorSet as = new AnimatorSet();
        int childCount = mStopList.getChildCount();
        ArrayList<Animator> list = new ArrayList<Animator>();
        for (int i = 0; i<childCount; ++i) {
            ImageView imv = (ImageView) mStopList.getChildAt(i).findViewById(R.id.object_realtime_prediction_selection_image);
            TextView tv = (TextView) mStopList.getChildAt(i).findViewById(R.id.object_realtime_prediction_time);

            if (IS_NORMAL_VIEW) {
                mAdapter.setSelectionMode(true);
                list.add(ObjectAnimator.ofFloat(imv, "x", imv.getRight(), imv.getRight()- imv.getWidth()));
                list.add(ObjectAnimator.ofFloat(tv, "x", tv.getLeft() + imv.getWidth(), tv.getLeft()));
            } else {
                mAdapter.setSelectionMode(false);
                list.add(ObjectAnimator.ofFloat(imv, "x", imv.getRight(), imv.getRight() + imv.getWidth()));
                list.add(ObjectAnimator.ofFloat(tv, "x", tv.getLeft(), tv.getLeft() + imv.getWidth()));
            }


        }
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAdapter.notifyDataSetChanged();
                IS_NORMAL_VIEW = !IS_NORMAL_VIEW;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        as.playTogether(list);
        as.setDuration(duration);
        as.start();
    }

    private void showAlreadyFavourite() {
        ((ImageView)findViewById(R.id.stop_details_favourite_icon)).setImageResource(R.drawable.favourite_star_selected);
        ((TextView)findViewById(R.id.stop_details_favourite_text)).setText("Unfavourite");
    }

    private void showMakeFavourite() {
        ((ImageView)findViewById(R.id.stop_details_favourite_icon)).setImageResource(R.drawable.favourite_star_unselected);
        ((TextView)findViewById(R.id.stop_details_favourite_text)).setText("Favourite");
    }

    private void showNoFiltersApplied() {
        ((ImageView)findViewById(R.id.stop_details_filter_icon)).setImageResource(R.drawable.filter_grey);
    }

    private void showFiltersApplied() {
        ((ImageView)findViewById(R.id.stop_details_filter_icon)).setImageResource(R.drawable.filter_green);
    }

    private void refreshAlarmIcon(){

        if(HugoPreferences.getActiveAlarm(this) == null){
            ((ImageView)findViewById(R.id.stop_details_alarm_icon)).setImageResource(R.drawable.alarm_grey);
            return;
        }

        if(HugoPreferences.getActiveAlarm(this).getAtcoCode().equalsIgnoreCase(mStop.getAtcoCode())){
            //This stop has an activeAlarm
            ((ImageView)findViewById(R.id.stop_details_alarm_icon)).setImageResource(R.drawable.alarm_green);
        }else{
            //This stop doesn't have an alarm
            ((ImageView)findViewById(R.id.stop_details_alarm_icon)).setImageResource(R.drawable.alarm_grey);
        }
    }

    private void showFiltersMenu() {
        menuItems.clear();

        Map<String, Integer> unfilteredNames = mStop.getAllUnFilteredServicesWithColourCode();
        for(String key : unfilteredNames.keySet()){
            SlideUpServiceItem item = new SlideUpServiceItem(StopDetailsActivity.this, key, Color.BLACK, unfilteredNames.get(key));
            menuItems.add(item);
        }

        Map<String, Integer> filteredNames = mStop.getAllFilteredServicesWithColourCode();
        for(String key : filteredNames.keySet()){
            SlideUpServiceItem item = new SlideUpServiceItem(StopDetailsActivity.this, key, Color.BLACK, filteredNames.get(key));
            item.unselect();
            menuItems.add(item);
        }

        SlideUpMenuItem selectAll = (new SlideUpMenuItem(StopDetailsActivity.this, "Select all routes", ContextCompat.getColor(this, R.color.blue_text_colour)));
        selectAll.setOnClickListener(v12 -> {
            slideUpMenu.selectAllServiceItems();
        });

        menuItems.add(selectAll);
        slideUpMenu.assignItems(menuItems);
        slideUpMenu.setOnCloseListener(view -> {
            mFilter = slideUpMenu.generateServiceFilter();
            mStop.applyFilterToStop(this, mFilter);
            mAdapter.refreshData(mStop.getFilteredPredictions(this));
            animateAllListClosed();
            if(mStop.stopHasActiveFilter(this)){
                showFiltersApplied();
            }else{
                showNoFiltersApplied();
            }
        });
        slideUpMenu.overrideCancelText("Accept changes");
        slideUpMenu.slideUp();
    }

    private void showDeleteAsFavouriteDialog() {

        CustomYesNoDialog dialog = new CustomYesNoDialog(this);
        dialog.setTitle("Remove " + mStop.getOverrideName())
                .setContentText("Are you sure you wish to remove this stop from your favourites list? please note it will not remove the stop from hugo only the favourites list so you can find it again if you need to.")
                .setAcceptButtonListener(() -> {
                    mDatabase.deleteFavouriteStop(mStop);
                    Toast.makeText(StopDetailsActivity.this, mStop.getOverrideName() + " removed as a favourite", Toast.LENGTH_SHORT).show();
                    showMakeFavourite();
                    return true;
                })
                .show();

    }

    private void startRefreshing() {

        if(mHandler == null){
            mHandler = new Handler();
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mRunnable = this;
                try{
                    mTask.cancel(true);
                }catch(Exception e){}

                RealTimeFullParams params = new RealTimeFullParams(false, StopDetailsActivity.this);
                params.addAtcoCode(mStop.getAtcoCode());
                mTask = new DataRequestTask(params);
                mTask.setOnTaskCompletedListener(StopDetailsActivity.this);
                mTask.execute(StopDetailsActivity.this);
                mHandler.postDelayed(mRunnable, 30 * 1000);
            }
        }, 0);

    }

    private void showSaveAsFavouriteDialog() {

        CustomEditTextDialog dialog = new CustomEditTextDialog(this);
        dialog.setTitle("Feel free to change the name of (" + mStop.getStopName() + ") to a name more personal to you")
              .setUserInputText(mStop.getStopName())
              .setAcceptButtonListener(() -> {
                  if(dialog.getEnteredText().isEmpty()){
                      dialog.setUserInputText(mStop.getStopName());
                      Toast.makeText(StopDetailsActivity.this, "You must provide a name", Toast.LENGTH_SHORT).show();
                      return false;
                  }else{
                      Database db = new Database(this);
                      mStop.setOverrideName(dialog.getEnteredText());
                      db.saveFavouriteStop(mStop, false);
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                          askToShowWidget();
                      }else{
                          showExplainWidgetDialog();
                      }
                      showAlreadyFavourite();
                      Toast.makeText(StopDetailsActivity.this, "Saved as a favourite", Toast.LENGTH_SHORT).show();
                      return true;
                  }
                })
                .show();

    }

    private void showExplainWidgetDialog() {

        int[] allids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, RealtimeWidgetProvider.class));
        if(allids != null && allids.length > 0){
            //There is already a widget on the home screen so do not show dialog
            return;
        }

        CustomImageDialog dialog = new CustomImageDialog(this)
                .setTitle("See your favourites even faster")
                .setContentText("hugo has a new widget which you can place on your home screen to see your favourite stops without having to open the app at all, long press on your home screen to see a list of available widgets")
                .setImage(R.drawable.widget_preview);
        dialog.show();

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(26)
    private void askToShowWidget() {
        AppWidgetManager mAppWidgetManager = this.getSystemService(AppWidgetManager.class);
        int[] allids = mAppWidgetManager.getAppWidgetIds(new ComponentName(this, RealtimeWidgetProvider.class));

        if(allids != null && allids.length > 0){
            //There is already a widget on the home screen so do not show dialog
            return;
        }



        ComponentName myProvider = new ComponentName(this, RealtimeWidgetProvider.class);

        if (mAppWidgetManager.isRequestPinAppWidgetSupported()) {
            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the widget to be pinned. Note that, if the pinning
            // operation fails, your app isn't notified.
            Intent pinnedWidgetCallbackIntent = new Intent(this, RealtimeWidgetProvider.class);
            PendingIntent successCallback;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                successCallback = PendingIntent.getBroadcast(this, 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            }else{
                successCallback = PendingIntent.getBroadcast(this, 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            mAppWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
        }
    }



    @Override
    public void onCompleted(Boolean bool) {

        try{
            mSwipeLayout.setRefreshing(false);

            if(bool){
                //task worked
                if(mTask.getResponse() != null && mTask.getResponse() instanceof ArrayList){
                    mStop.setPredictions((ArrayList<RealtimePrediction>) mTask.getResponse());
                    selectedPredictions.clear();
                    selectedPredictions.addAll(mStop.getPredictions());
                    mAdapter.refreshData(mStop.getFilteredPredictions(this));
                }
            }
        }catch(Exception e){}

    }
}
