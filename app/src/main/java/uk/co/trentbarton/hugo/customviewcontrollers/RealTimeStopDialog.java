package uk.co.trentbarton.hugo.customviewcontrollers;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

import uk.co.trentbarton.hugo.activities.StopDetailsActivity;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DataRequestParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.dialogs.CustomEditTextDialog;
import uk.co.trentbarton.hugo.dialogs.CustomImageDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.OnPredictionClickedListener;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.interfaces.PlainOnClickListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.widget.RealtimeWidgetProvider;

public class RealTimeStopDialog extends RelativeLayout implements OnTaskCompletedListener{

    private DateTime mLastTimeShown;
    private TextView stopName;
    private ImageView favouriteStar, rightArrow;
    private LinearLayout contentHolder, filtersAppliedIcon;
    private ProgressBar progressBar;
    private Stop mStop;
    private TextView emptyMessage;
    private Handler mHandler;
    private int delay = 30000; //30 seconds
    private Runnable runnable;
    private DataRequestTask task;
    private OnPredictionClickedListener mListener;
    private boolean isFavourite = false;
    private PlainOnClickListener mFavouriteClickedListener;

    public RealTimeStopDialog(Context context) {
        super(context);
        init();
    }

    public RealTimeStopDialog(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RealTimeStopDialog(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RealTimeStopDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOnFavouriteClickedListener(PlainOnClickListener l){
        mFavouriteClickedListener = l;
    }

    public void setPredictionClickedListener(OnPredictionClickedListener l){
        mListener = l;
    }

    private void init(){
        this.hideDialog();
        View view = inflate(getContext(), R.layout.object_live_stop_realtime_dialog, this);
        this.stopName = view.findViewById(R.id.live_dialog_stop_name_text);
        this.contentHolder = view.findViewById(R.id.live_dialog_content_holder);
        this.favouriteStar = view.findViewById(R.id.live_dialog_favourite_icon);
        this.progressBar = view.findViewById(R.id.live_dialog_empty_progress_bar);
        this.filtersAppliedIcon = view.findViewById(R.id.live_dialog_filters_applied);
        this.emptyMessage = view.findViewById(R.id.live_dialog_empty_message);
        this.rightArrow = view.findViewById(R.id.live_dialog_stop_info_enter);
        mHandler = new Handler();

        rightArrow.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StopDetailsActivity.class);
            intent.putExtra("stop",mStop);
            getContext().startActivity(intent);
        });

        this.favouriteStar.setOnClickListener( v ->{
            favouriteClicked();
        });

    }

    private void favouriteClicked() {

        if(isFavourite){
            //Show dialog asking to remove as a favourite
            CustomYesNoDialog dialog = new CustomYesNoDialog(getContext())
                    .setTitle("Remove as favourite?")
                    .setContentText(String.format(Locale.ENGLISH,"Do you wish to unfavourite %s?",mStop.getOverrideName()))
                    .setAcceptButtonListener(() -> {
                        Database db = new Database(getContext());
                        db.deleteFavouriteStop(mStop);
                        isFavourite = false;
                        this.favouriteStar.setImageResource(R.drawable.favourite_star_unselected);
                        if(mFavouriteClickedListener != null){
                            mFavouriteClickedListener.onClick();
                        }
                        return true;
                    });
            dialog.show();
        }else{
            //show dialog asking to save as a favourite
            CustomEditTextDialog dialog = new CustomEditTextDialog(getContext());
            dialog.setTitle("Feel free to change the name of (" + mStop.getStopName() + ") to a name more personal to you")
                    .setUserInputText("")
                    .setHintText(mStop.getStopName())
                    .setAcceptButtonListener(() -> {

                        String newName = dialog.getEnteredText();
                        if(dialog.getEnteredText().isEmpty()){
                            newName = mStop.getStopName();
                        }

                        Database db = new Database(getContext());
                        mStop.setOverrideName(newName);
                        db.saveFavouriteStop(mStop, false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            askToShowWidget();
                        }else{
                            showExplainWidgetDialog();
                        }
                        isFavourite = true;
                        this.favouriteStar.setImageResource(R.drawable.favourite_star_selected);
                        Toast.makeText(getContext(), "Saved as a favourite", Toast.LENGTH_SHORT).show();
                        if(mFavouriteClickedListener != null){
                            mFavouriteClickedListener.onClick();
                        }
                        return true;
                    })
                    .show();
        }
    }

    private void showExplainWidgetDialog() {

        int[] allids = AppWidgetManager.getInstance(getContext()).getAppWidgetIds(new ComponentName(getContext(), RealtimeWidgetProvider.class));
        if(allids != null && allids.length > 0){
            //There is already a widget on the home screen so do not show dialog
            return;
        }

        CustomImageDialog dialog = new CustomImageDialog(getContext())
                .setTitle("See your favourites even faster")
                .setContentText("hugo has a new widget which you can place on your home screen to see your favourite stops without having to open the app at all, long press on your home screen to see a list of available widgets")
                .setImage(R.drawable.widget_preview);
        dialog.show();

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void askToShowWidget() {
        AppWidgetManager mAppWidgetManager = getContext().getSystemService(AppWidgetManager.class);
        int[] allids = mAppWidgetManager.getAppWidgetIds(new ComponentName(getContext(), RealtimeWidgetProvider.class));

        if(allids != null && allids.length > 0){
            //There is already a widget on the home screen so do not show dialog
            return;
        }



        ComponentName myProvider = new ComponentName(getContext(), RealtimeWidgetProvider.class);

        if (mAppWidgetManager.isRequestPinAppWidgetSupported()) {
            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the widget to be pinned. Note that, if the pinning
            // operation fails, your app isn't notified.
            Intent pinnedWidgetCallbackIntent = new Intent(getContext(), RealtimeWidgetProvider.class);
            PendingIntent successCallback;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                successCallback = PendingIntent.getBroadcast(getContext(), 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            }else{
                successCallback = PendingIntent.getBroadcast(getContext(), 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            mAppWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
        }
    }

    private void setAsEmpty(){
        this.emptyMessage.setVisibility(VISIBLE);
        this.contentHolder.setVisibility(GONE);
        this.progressBar.setVisibility(GONE);
    }

    private void setAsSearchingNoData(){
        this.progressBar.setVisibility(VISIBLE);
        this.emptyMessage.setVisibility(GONE);
        this.contentHolder.setVisibility(GONE);
    }

    private void setContent(ArrayList<RealtimePrediction> predictions){

        this.mStop.setPredictions(predictions);
        predictions = this.mStop.getFilteredPredictions(getContext());

        if(predictions == null || predictions.size() == 0){
            setAsEmpty();
            return;
        }

        this.progressBar.setVisibility(GONE);
        this.emptyMessage.setVisibility(GONE);
        this.contentHolder.setVisibility(VISIBLE);
        this.contentHolder.removeAllViewsInLayout();

        int counter = 0;

        while (counter < 3 && counter < predictions.size()){

            RealtimePrediction p = predictions.get(counter);
            View view = inflate(getContext(), R.layout.object_realtime_prediction_item, null);
            TextView serviceName = view.findViewById(R.id.object_realtime_prediction_service_name);
            TextView serviceDestination = view.findViewById(R.id.object_realtime_prediction_destination_name);
            TextView predictionTime = view.findViewById(R.id.object_realtime_prediction_time);
            TextView bottomLine = view.findViewById(R.id.object_realtime_bottom_line);
            ImageView serviceColour = view.findViewById(R.id.object_realtime_prediction_service_colour);

            serviceName.setText(p.getServiceName());
            serviceDestination.setText(p.getJourneyDestination());
            predictionTime.setText(p.getFormattedPredictionDisplay());
            serviceColour.setBackgroundColor(p.getServiceColour());

            if(counter == 2 || counter == predictions.size() - 1){
                bottomLine.setVisibility(GONE);
            }else{
                bottomLine.setVisibility(VISIBLE);
            }

            view.setOnClickListener(v -> {
                if(mListener != null){
                    mListener.OnPredictionClicked(p);
                }
            });

            this.contentHolder.addView(view);

            counter++;
        }

    }

    public void showDialog(Stop stop){

        mStop = stop;
        mStop.resetFilter(getContext());

        if(mStop.stopHasActiveFilter(getContext())){
            filtersAppliedIcon.setVisibility(View.VISIBLE);
        }else{
            filtersAppliedIcon.setVisibility(View.GONE);
        }

        this.stopName.setText(stop.getStopName());
        setAsSearchingNoData();
        //Start the task to refresh or cancel if there is an existing one and restart

        Database db = new Database(getContext());
        if(db.isStopFavourite(mStop.getAtcoCode())){
            this.favouriteStar.setImageResource(R.drawable.favourite_star_selected);
            isFavourite = true;
        }else{
            isFavourite = false;
            this.favouriteStar.setImageResource(R.drawable.favourite_star_unselected);
        }

        RealTimeFullParams params = new RealTimeFullParams(false, getContext());
        params.addAtcoCode(stop.getAtcoCode());
        if(params.validate()){
            startTask(params);
        }
        mLastTimeShown = DateTime.now();
        this.setVisibility(VISIBLE);
    }

    public void hideDialog(){

        if(mHandler != null && runnable!= null){
            mHandler.removeCallbacks(runnable);
        }

        if(mLastTimeShown == null){
            this.setVisibility(GONE);
        }else if(mLastTimeShown.isBefore(DateTime.now().minusMillis(200))){
            this.setVisibility(GONE);
        }
    }

    private void startTask(DataRequestParams params){

        mHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                runnable = this;
                try{
                    task.cancel(true);
                }catch(Exception e){}
                task = new DataRequestTask(params);
                task.setOnTaskCompletedListener(RealTimeStopDialog.this);
                task.execute(getContext());
                mHandler.postDelayed(runnable, delay);
            }
        }, 0);

    }

    @Override
    public void onCompleted(Boolean result) {

        if(!result){
            //task failed for some reason
            setAsEmpty();
        }

        ArrayList<RealtimePrediction> predictions = new ArrayList<>();
        Object obj = task.getResponse();

        if(obj instanceof ArrayList){

            if(((ArrayList) obj).size() == 0){
                setAsEmpty();
                return;
            }

            for(Object object : ((ArrayList) obj)){

                if(object instanceof RealtimePrediction){
                    predictions.add((RealtimePrediction) object);
                }
            }
        }

        setContent(predictions);

    }
}
