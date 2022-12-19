package uk.co.trentbarton.hugo.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.MainNavigationActivity;
import uk.co.trentbarton.hugo.customview.CustomDoghnut;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;


public class RealtimeWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = RealtimeWidgetRemoteViewFactory.class.getSimpleName();
    List<RealtimePrediction> mCollection;
    Context mContext;
    private int appWidgetId;

    public RealtimeWidgetRemoteViewFactory(Context context, Intent intent) {
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mContext = context;
        mCollection = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mCollection.clear();
        mCollection.addAll(HugoPreferences.getWidgetRealtimePredictions(mContext));
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCollection.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.object_realtime_prediction_item);
        RealtimePrediction prediction = null;

        try {
            prediction = mCollection.get(position);
        }catch(Exception e){
            Log.e(TAG, "Error obtaining prediction from collection", e);
            return view;
        }
        view.setTextViewText(R.id.object_realtime_prediction_service_name, prediction.getServiceName());
        view.setTextViewText(R.id.object_realtime_prediction_destination_name, prediction.getJourneyDestination());
        view.setTextViewText(R.id.object_realtime_prediction_time, prediction.getFormattedPredictionDisplay());
        view.setViewVisibility(R.id.object_realtime_bottom_line, View.GONE);
        view.setImageViewBitmap(R.id.object_realtime_prediction_service_colour, new CustomDoghnut(prediction.getServiceColour()).getBitmap(mContext));

        Intent intent = new Intent(mContext, MainNavigationActivity.class);
        intent.putExtra("function", RealtimeWidgetProvider.FUNCTION_PREDICTION_CLICKED);
        intent.putExtra("atcoCode", prediction.getStopCode());
        intent.putExtra("vehicleNumber", prediction.getVehicleNumber());
        view.setOnClickFillInIntent(R.id.background, intent);
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}