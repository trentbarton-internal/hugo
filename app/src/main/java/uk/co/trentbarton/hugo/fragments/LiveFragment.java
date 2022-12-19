package uk.co.trentbarton.hugo.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.firebase.installations.FirebaseInstallations;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.util.ArrayList;
import java.util.Locale;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.OnBoardingActivity;
import uk.co.trentbarton.hugo.activities.StopSearchActivity;
import uk.co.trentbarton.hugo.customview.BusTrackingDialog;
import uk.co.trentbarton.hugo.customview.FavouriteBusStopList;
import uk.co.trentbarton.hugo.customview.NearbyBusStopList;
import uk.co.trentbarton.hugo.customview.RefreshView;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.customviewcontrollers.RealTimeStopDialog;
import uk.co.trentbarton.hugo.customviewcontrollers.RefreshViewController;
import uk.co.trentbarton.hugo.dataholders.Alarm;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.CreateAlarmParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DeleteAlarmParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.SendPushTokenParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomSetAlarmDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.MapHelperListener;
import uk.co.trentbarton.hugo.interfaces.OnPredictionClickedListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.tools.Tools;

public class LiveFragment extends MapFragment implements OnPredictionClickedListener{

    private RefreshView mTrackingRefreshView;
    private RefreshViewController mTrackingViewController;
    private NearbyBusStopList mNearByBusStopList;
    private FavouriteBusStopList mFavouriteBusStopList;
    private RelativeLayout mFullScreenContentHolder;
    private LinearLayout mSearchForStopButton;
    private BusTrackingDialog mBusTrackingDialog;
    private RealTimeStopDialog mStopDialog;
    private RelativeLayout addStopsButton, alarmSetFlash, alarmRemovedFlash;
    private ImageView mCloseTrackingButton;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private LinearLayout mZoomInHintMessage, mTrackingButtonsHolder;
    private RealtimePrediction mLastBusTapped;
    private boolean firstTimeShowingDialog = true;
    private boolean busDialogShowing = false;
    private LinearLayout mRealtimeBanner, mSetAlarmButton;
    private ImageView mServiceRing;
    private TextView mServiceName, mPredictionText;
    private RelativeLayout mAlarmSetProgress;
    private boolean onResumeCalled = false;
    private String TAG = this.getClass().getSimpleName();

    public static LiveFragment newInstance() {
        LiveFragment fragment = new LiveFragment();
        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(LAYOUT_REFERENCE, R.layout.fragment_live_main);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = getBaseView();

        assignViews(view);
        setupListeners();
        setUpSearchingState();
        MapHelper.getInstance().assignContext(getContext());
        MapHelper.getInstance().setmListener(new MapHelperListener() {
            @Override
            public void onMapStateChanged() {
                LiveFragment.this.onMapStateChanged();
            }

            @Override
            public void onBusClicked(RealtimePrediction prediction) {
                LiveFragment.this.onBusClicked(prediction);
            }

            @Override
            public void onStopClicked(Stop stop) {
                LiveFragment.this.onStopClicked(stop);
            }
        });

        reOrderMenus(view);
        moveToCurrentLocation();
        setupFavouriteStops();
        return view;

    }

    public void onMapStateChanged() {
        if(MapHelper.getInstance().getMapState() == MapHelper.MapState.ZOOMED_OUT){
            setUpZoomedOutState();
        }else if(MapHelper.getInstance().getMapState() == MapHelper.MapState.SEARCHING){
            setUpSearchingState();
        }else{
            setUpTrackingState();
        }
    }

    public void onBusClicked(RealtimePrediction prediction) {
        firstTimeShowingDialog = true;
        showBusDialog(prediction);
    }

    public void onStopClicked(Stop stop) {
        showRealTimeDialog(stop);
    }

    private void assignViews(View view){

        mTrackingRefreshView = view.findViewById(R.id.live_main_tracking_refresh_view);
        mTrackingViewController = new RefreshViewController();
        mTrackingViewController.setRefreshView(mTrackingRefreshView);
        mSlidingUpPanel = view.findViewById(R.id.live_sliding_layout);
        mNearByBusStopList = view.findViewById(R.id.live_nearby_bus_stop_view_holder);
        mSearchForStopButton = view.findViewById(R.id.live_search_for_stop);
        mStopDialog = view.findViewById(R.id.live_realtime_dialog);
        mZoomInHintMessage = view.findViewById(R.id.live_zoom_to_see_stops_notification);
        mCloseTrackingButton = view.findViewById(R.id.live_close_bus_tracking_button);
        mTrackingButtonsHolder = view.findViewById(R.id.live_bus_tracking_button_holder);
        mFullScreenContentHolder = view.findViewById(R.id.live_main_content_whole_screen);
        mBusTrackingDialog = new BusTrackingDialog(getContext(), mFullScreenContentHolder);
        addStopsButton = view.findViewById(R.id.customer_live_favourite_add_stops);
        mFavouriteBusStopList = view.findViewById(R.id.live_favourite_bus_stop_view_holder);
        mRealtimeBanner = view.findViewById(R.id.fragment_live_banner);
        mSetAlarmButton = view.findViewById(R.id.fragment_live_main_set_alarm_button);
        mServiceRing = view.findViewById(R.id.fragment_live_main_banner_service_ring);
        mServiceName = view.findViewById(R.id.fragment_live_main_banner_service_name);
        mPredictionText = view.findViewById(R.id.fragment_live_main_banner_prediction_text);
        alarmSetFlash = view.findViewById(R.id.fragment_live_alarm_set_screen);
        alarmRemovedFlash = view.findViewById(R.id.fragment_live_alarm_removed_screen);
        mAlarmSetProgress = view.findViewById(R.id.fragment_live_alarm_progress);
    }

    public void setupFavouriteStops(){
        //Call this every time we need to generate favourite stops
        if(getContext() != null){
            mFavouriteBusStopList.refreshView();
        }
    }

    public void setupNearbyStops(){
        //Call this every time we need to generate favourite stops
        if(getContext() != null){
            mNearByBusStopList.refreshView();
        }
    }

    private void setupListeners(){

        mSearchForStopButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StopSearchActivity.class);
            startActivity(intent);
        });

        mSetAlarmButton.setOnClickListener( v -> {
            setAlarm();
        });

        addStopsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OnBoardingActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        if(!haveAccessToLocation()){
            mNearByBusStopList.setLocationAvailable(false);
            mNearByBusStopList.refreshView();
        }else{
            mNearByBusStopList.setLocationAvailable(true);
        }

        mStopDialog.setOnFavouriteClickedListener(() -> {
            mFavouriteBusStopList.refreshView();
        });

        new Handler().postDelayed(super::setLocationUpdates, 1000);

        if(mTrackingViewController != null){
            mTrackingViewController.setListener(o -> {

                if(getActivity() == null){
                    return;
                }

                getActivity().runOnUiThread(() -> {

                    ArrayList<RealtimePrediction> predictions = (ArrayList<RealtimePrediction>)o;
                    if(predictions == null){
                        return;
                    }

                    MapHelper.getInstance().trackingRefreshed(predictions);

                    for(RealtimePrediction prediction : predictions){

                        if(MapHelper.getInstance().getTrackedPrediction() == null || prediction == null){
                            break;
                        }

                        if(MapHelper.getInstance().getTrackedPrediction().equals(prediction)){
                            updateRealtimeBanner(prediction);
                            break;
                        }
                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        super.setLocationUpdates();
                        if(busDialogShowing && mLastBusTapped != null){

                            for(RealtimePrediction prediction : predictions){
                                if(prediction.equals(mLastBusTapped)){
                                    showBusDialog(prediction);
                                    return;
                                }
                            }
                            hideBusDialog();
                        }

                    }, 100);
                });
            });
        }

        mTrackingRefreshView.setOnClickListener(v -> {
            mTrackingViewController.refreshNow();
        });

        mCloseTrackingButton.setOnClickListener((e)->{
            setUpSearchingState();
            MapHelper.getInstance().endTracking();
        });

        mNearByBusStopList.setOnPredictionClickedListner(this);
        mFavouriteBusStopList.setOnPredictionClickedListner(this);
        mStopDialog.setPredictionClickedListener(this);
    }

    private void setAlarm(){

        Alarm alarm = HugoPreferences.getActiveAlarm(getContext());

        if(Tools.areNotificationsBlocked(getContext())){
            CustomYesNoDialog dialog = new CustomYesNoDialog(getContext());
            dialog.setTitle("Notifications are blocked");
            dialog.setContentText("For this feature to work notifications must be enabled, do you want to change these settings?");
            dialog.setAcceptButtonListener(() -> {
                Tools.openNotificationSettings(LiveFragment.this.getContext());
                return true;
            });
            dialog.show();
            return;
        }

        if(alarm != null){
            //We already have an alarm set, prompt user to delete it.
            CustomYesNoDialog dialog = new CustomYesNoDialog(getContext())
                    .setTitle("Alarm already set")
                    .setContentText(String.format(Locale.ENGLISH,"You already have an active alarm set for %s at %s, do you want to remove it?", alarm.getServiceName(), alarm.getScheduledTime().toString("HH:mm")));
            dialog.setAcceptButtonListener(() -> {

                DeleteAlarmParams mParams = new DeleteAlarmParams(getContext());
                mParams.addAlarm(HugoPreferences.getActiveAlarm(getContext()));
                DataRequestTask task = new DataRequestTask(mParams);
                task.setOnTaskCompletedListener(successful -> {
                    if(successful){
                        HugoPreferences.setActiveAlarm(getContext(), null);
                        showAlarmRemovedNotification();
                    }else{
                        Toast.makeText(getContext(), "Failed to remove alarm from the server", Toast.LENGTH_SHORT).show();
                    }
                });
                task.execute(getContext());
                return true;
            });
            dialog.show();
            return;
        }

        if(!validateSelectedVehicle()){
            return;
        }

        //It's a valid trip so show the dialog with a number picker in it
        CustomSetAlarmDialog dialog = new CustomSetAlarmDialog(getContext())
                .setMinNumber(5)
                .setMaxNumber(((int)Math.floor(MapHelper.getInstance().getTrackedPrediction().getPredictionInSeconds() / 60)) - 1);
        dialog.setAcceptButtonListener(() -> {
            int number = dialog.getSelectedNumber();
            runSetAlarmTask(number);
            return true;
        });
        dialog.show();
    }


    private void checkPushSent(int number){

        FirebaseInstallations.getInstance().getId().addOnSuccessListener(getActivity(), newToken -> {
            Log.d(TAG, "Server token:" + newToken);
            HugoPreferences.setPushToken(getContext(),newToken);
            HugoPreferences.setPushSent(getContext(), false);
            SendPushTokenParams params = new SendPushTokenParams(getContext());
            params.setPushToken(newToken);
            DataRequestTask task = new DataRequestTask(params);
            task.setOnTaskCompletedListener(result -> {
                if(result){
                    Log.d(TAG, "Token successfully sent to server");
                    HugoPreferences.setPushSent(getContext(), true);
                }else{
                    Log.d(TAG, "Token update to hugo servers failed because " + task.getErrorMessage());
                }
                runSetAlarmTask(number);
            });
            task.execute(getContext());
        });
    }


    private void runSetAlarmTask(int number) {

        showAlarmLoading();

        if((!HugoPreferences.isPushSent(getContext())) || HugoPreferences.getPushToken(getContext()).isEmpty()){
            checkPushSent(number);
            return;
        }


        CreateAlarmParams mParams = new CreateAlarmParams(getContext());
        Alarm alarm = new Alarm();
        alarm.setMinuteTrigger(number);
        alarm.setScheduledTime(MapHelper.getInstance().getTrackedPrediction().getScheduledDepartureTime());
        alarm.setServiceName(MapHelper.getInstance().getTrackedPrediction().getServiceName());
        alarm.setAtcoCode(MapHelper.getInstance().getTrackedPrediction().getStopCode());
        if(MapHelper.getInstance().getTrackedStop() != null){
            alarm.setStopName(MapHelper.getInstance().getTrackedStop().getOverrideName());
        }else{
            alarm.setStopName("Unknown stop");
        }

        mParams.addAlarm(alarm);
        DataRequestTask task = new DataRequestTask(mParams);
        task.setOnTaskCompletedListener(result -> {
            if(result && task.getResponse() != null){
                alarm.setAlarmID((Integer)task.getResponse());
                HugoPreferences.setActiveAlarm(getContext(), alarm);
                hideAlarmLoading();
                showAlarmSetNotification();
            }else{
                Toast.makeText(getContext(), "Failed to register alarm with server", Toast.LENGTH_LONG).show();
            }
        });

        task.execute(getContext());
    }

    private void showAlarmLoading(){
        mAlarmSetProgress.setVisibility(View.VISIBLE);
    }

    private void hideAlarmLoading(){
        mAlarmSetProgress.setVisibility(View.GONE);
    }

    private void showAlarmSetNotification(){
        alarmSetFlash.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            alarmSetFlash.setVisibility(View.GONE);
        }, 1500);
    }

    private void showAlarmRemovedNotification(){
        alarmRemovedFlash.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            alarmRemovedFlash.setVisibility(View.GONE);
        }, 1500);
    }

    private boolean validateSelectedVehicle() {

        RealtimePrediction selectedPrediction = MapHelper.getInstance().getTrackedPrediction();

        if(selectedPrediction == null){
            Toast.makeText(getContext(), "Oops... something hasn't worked right please try to select again.", Toast.LENGTH_LONG).show();
            return false;
        }

        if(!selectedPrediction.isWorking()){
            Toast.makeText(getContext(), "Sorry the realtime isn't working on this vehicle, predictions that show clock face times are timetabled departures", Toast.LENGTH_LONG).show();
            return false;
        }

        if(selectedPrediction.getPredictionInSeconds() < 5 * 60){
            Toast.makeText(getContext(), "You can't set an alarm for a service due within 5 minutes", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void setUpTrackingState() {

        mRealtimeBanner.setVisibility(View.VISIBLE);

        updateRealtimeBanner(MapHelper.getInstance().getTrackedPrediction());

        if(MapHelper.getInstance().getLastStopClicked() != null){

            Stop stop = MapHelper.getInstance().getLastStopClicked();
            RealTimeFullParams params = new RealTimeFullParams(false, getContext()).addAtcoCode(stop.getAtcoCode());
            mTrackingViewController.setDataRequestParams(params);
            mTrackingViewController.startRefreshing();
            mTrackingViewController.refreshNow();

        }

        //remove all searching views
        this.mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        this.mZoomInHintMessage.setVisibility(View.GONE);
        this.mSearchForStopButton.setVisibility(View.GONE);
        this.mTrackingButtonsHolder.setVisibility(View.VISIBLE);
        //Make the stopDialog be higher
        this.mStopDialog.animate().translationY(-Metrics.densityPixelsToPixels(50)).setDuration(100).start();

    }

    private void updateRealtimeBanner(RealtimePrediction prediction){

        if(prediction == null){
            mServiceName.setText("waiting...");
            mPredictionText.setText("");
        }else{
            mServiceName.setText(prediction.getServiceName() + " - " + prediction.getJourneyDestination());
            mPredictionText.setText(prediction.getFormattedPredictionDisplay());
        }

        try{

            int x = Metrics.densityPixelsToPixels(50);

            Bitmap temp = Bitmap.createBitmap(x, x, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(Metrics.densityPixelsToPixels(9));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(prediction.getServiceColour());
            canvas.drawCircle(x/2, x/2, x * 0.4f, paint);

            mServiceRing.setImageDrawable(new BitmapDrawable(getResources(), temp));

        }catch(Exception ignore){
            //Can happen if we navigate back here onRefresh and call getResources before we have context
            mServiceRing.setImageBitmap(null);
        }
    }

    private void setUpSearchingState(){

        mRealtimeBanner.setVisibility(View.GONE);
        mTrackingViewController.stopRefreshing();
        setupNearbyStops();
        setupFavouriteStops();


        mZoomInHintMessage.setVisibility(View.GONE);
        this.mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        this.mSearchForStopButton.setVisibility(View.VISIBLE);
        this.mTrackingButtonsHolder.setVisibility(View.GONE);
        this.mStopDialog.animate().translationY(-Metrics.densityPixelsToPixels(0)).setDuration(100).start();
        mBusTrackingDialog.hideDialog();

    }

    private void setUpZoomedOutState(){
        mZoomInHintMessage.setVisibility(View.VISIBLE);
        hideRealTimeDialog();
        hideBusDialog();
    }

    @Override
    public void OnPredictionClicked(RealtimePrediction prediction) {

        //This is called whenever the user taps on a prediction anywhere
        if(!prediction.isWorking()){
            try{
                Toast.makeText(getContext(), "The GPS signal isn't working right now for this vehicle, please try later", Toast.LENGTH_LONG).show();
            }catch (Exception ignore){
            }
            return;
        }

        Database db = new Database(getContext());
        Stop stop = db.getStopFromStopCode(prediction.getStopCode());

        if(stop != null){
            MapHelper.getInstance().setLastStopClicked(stop);
        }

        hideRealTimeDialog();
        MapHelper.getInstance().assignMonitoredVehicle(prediction);

        if(MapHelper.getInstance().getMapState() == MapHelper.MapState.TRACKING_VEHICLES){
            updateRealtimeBanner(prediction);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!onResumeCalled){
            onResumeCalled = true;
            MapHelper.getInstance().onResume();
            restartFavouriteStops();
            restartNearbyStops();
            if(mTrackingViewController != null && MapHelper.getInstance().getMapState() == MapHelper.MapState.TRACKING_VEHICLES) mTrackingViewController.startRefreshing();
            MapHelper.getInstance().forceFakeMapMove();
        }
    }

    @Override
    public void onPause() {
        onResumeCalled = false;
        MapHelper.getInstance().onPause();
        try{
            mTrackingViewController.stopRefreshing();
            pauseFavouriteStops();
            pauseNearbyStops();
            hideRealTimeDialog();
        }catch(Exception ignore){}
        super.onPause();
    }

    private void pauseFavouriteStops() {

        if(mFavouriteBusStopList != null){
            mFavouriteBusStopList.pauseSearching();
        }
    }

    private void pauseNearbyStops(){
        if(mNearByBusStopList != null){
            mNearByBusStopList.pauseSearching();
        }
    }

    private void restartFavouriteStops(){
        if(mFavouriteBusStopList != null){
            mFavouriteBusStopList.restartSearching();
        }
    }

    private void restartNearbyStops(){
        if(mNearByBusStopList != null){
            mNearByBusStopList.restartSearching();
        }
    }

    public void reOrderMenus(View view){

        Context context = getContext();
        if(context == null){return;}

        Database db = new Database(context);
        ArrayList<Stop> favStops = db.getFavouriteStops();

        if(favStops != null && favStops.size() > 0){

            LinearLayout holder = view.findViewById(R.id.live_content_holder);
            LinearLayout nearbyHolder = view.findViewById(R.id.customer_live_nearby_card);
            holder.removeView(nearbyHolder);
            holder.addView(nearbyHolder);

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        MapHelper.getInstance().assignMap(googleMap);
        moveToCurrentLocation();
        MapHelper.getInstance().forceFakeMapMove();


        googleMap.setOnCameraMoveListener(() -> {
            hideRealTimeDialog();
            if(USER_MOVED_MAP){hideBusDialog();}
            MapHelper.getInstance().mapMoved();
            shrinkSlidingPane();
        });

        googleMap.setOnMapClickListener(latLng -> {
            hideRealTimeDialog();
            shrinkSlidingPane();
        });
    }

    private void showBusDialog(RealtimePrediction prediction) {

        Projection projection = getMap().getProjection();
        Point busPosition = projection.toScreenLocation(prediction.getVehiclePosition());
        mBusTrackingDialog.showDialog(busPosition, prediction);
        busDialogShowing = true;
        mLastBusTapped = prediction;

        if(firstTimeShowingDialog){
            firstTimeShowingDialog = false;
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                hideBusDialog();
                showBusDialog(prediction);

            }, 50);
        }
    }

    private void hideBusDialog(){
        mBusTrackingDialog.hideDialog();
        busDialogShowing = false;
    }

    private void showRealTimeDialog(Stop stop) {

        //Find the stop to which this marker refers to
        hideBusDialog();
        this.mStopDialog.showDialog(stop);
    }

    private void hideRealTimeDialog(){
        mStopDialog.hideDialog();
    }

    private void shrinkSlidingPane(){

        if(MapHelper.getInstance().getMapState() != MapHelper.MapState.TRACKING_VEHICLES){
            mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @Override
    protected void moveToCurrentLocation() {
        //If we can't consume the event here call from super class
        if(!MapHelper.getInstance().moveMapToBestLocation()){
            super.moveToCurrentLocation();
        }
    }

    @Override
    protected boolean setCurrentLocation(Location l) {
        if(super.setCurrentLocation(l) || !mNearByBusStopList.isParamsSet()){
            if(l != null){
                mNearByBusStopList.updateLocation(l);
            }else{
                mNearByBusStopList.setLocationAvailable(false);
                mNearByBusStopList.refreshView();
            }
        }
        return true;
    }

    public void setWidgetData(String atcoCode, int vehicleNumber, Context context) {

        RealTimeFullParams mParams = new RealTimeFullParams(false, context);
        mParams.addAtcoCode(atcoCode);
        DataRequestTask task = new DataRequestTask(mParams);
        task.setOnTaskCompletedListener(bool -> {
            if(bool){
                if(task.getResponse() instanceof ArrayList){
                    ArrayList<RealtimePrediction> predictions = (ArrayList<RealtimePrediction>)task.getResponse();
                    for(RealtimePrediction rp : predictions){
                        if(rp.getVehicleNumber() == vehicleNumber){
                            OnPredictionClicked(rp);
                            return;
                        }
                    }
                }
            }
        });
        task.execute(getContext());

    }

    @Override
    public void onLocationChanged(Location location) {
        if(super.setCurrentLocation(location)){
            moveToCurrentLocation();
            MapHelper.getInstance().forceFakeMapMove();
            mNearByBusStopList.updateLocation(location);
        }
    }
}
