package uk.co.trentbarton.hugo.widget;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.MainNavigationActivity;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public class RealtimeWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_SHOW_DATA = "uk.co.trentbarton.hugo.action_show_data";
    public static final String ACTION_NEXT_HIT = "uk.co.trentbarton.hugo.action_next_hit";
    public static final String ACTION_NO_NETWORK = "uk.co.trentbarton.hugo.action_no_network";
    public static final String ACTION_BACK_HIT = "uk.co.trentbarton.hugo.action_back_hit";
    public static final String ACTION_REFRESH_HIT = "uk.co.trentbarton.hugo.action_refresh_hit";
    private static final String TAG = RealtimeWidgetProvider.class.getSimpleName();
    public static final String FUNCTION_PREDICTION_CLICKED = "uk.co.trentbarton.hugo.widget.predictionClicked";

    @Override
    public void onEnabled(Context context) {
        int[] allids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, RealtimeWidgetProvider.class));
        onUpdate(context, AppWidgetManager.getInstance(context), allids);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(TAG, "onUpdate called...");

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
            Intent serviceIntent = new Intent(context,RealtimeWidgetRemoteViewsService.class);
            views.setRemoteAdapter(R.id.widgetResultsList, serviceIntent);
            views.setOnClickPendingIntent(R.id.widgetBackButton, getBackButtonHitIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.widgetRefreshButton, getRefreshButtonHitIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.widgetNextButton, getNextButtonHitIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.widgetNoFavouritesView, getOpenHugoHitIntent(context));
            showNoData(views);

            Database db = new Database(context);
            ArrayList<Stop> stops = db.getFavouriteStops();

            if(stops == null || stops.isEmpty()){
                HugoPreferences.setWidgetCurrentStopName(context, "");
                views.setTextViewText(R.id.widgetStopText, "");
                HugoPreferences.setWidgetRealtimePredictions(context, "", null);
                showNoFavouritesSet(views);
            }else{
                if(getCurrentStopName(context).equals("")) {
                    HugoPreferences.setWidgetCurrentStopName(context, stops.get(0).getOverrideName());
                    views.setTextViewText(R.id.widgetStopText, stops.get(0).getOverrideName());
                }else {
                    views.setTextViewText(R.id.widgetStopText, getCurrentStopName(context));
                }
            }

            Intent startActivityIntent = new Intent(context, MainNavigationActivity.class);

            PendingIntent startActivityPendingIntent;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
            }else{
                startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            views.setPendingIntentTemplate(R.id.widgetResultsList, startActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

        //super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private PendingIntent getNextButtonHitIntent(Context context, int appWidgetId){
        Intent intent = new Intent(context, RealtimeWidgetProvider.class);
        intent.setAction(ACTION_NEXT_HIT);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        }else{
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


    }

    private PendingIntent getBackButtonHitIntent(Context context, int appWidgetId){
        Intent intent = new Intent(context, RealtimeWidgetProvider.class);
        intent.setAction(ACTION_BACK_HIT);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        }else{
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private PendingIntent getRefreshButtonHitIntent(Context context, int appWidgetId){
        Intent intent = new Intent(context, RealtimeWidgetProvider.class);
        intent.setAction(ACTION_REFRESH_HIT);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        }else{
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private PendingIntent getOpenHugoHitIntent(Context context){
        Intent intent = new Intent(context, MainNavigationActivity.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        }else{
            return PendingIntent.getActivity(context, 0, intent, 0);
        }

    }

    private void updateStopNameText(RemoteViews views, Context context, String text){
        Log.d(TAG, "Stop Name called");
        views.setTextViewText(R.id.widgetStopText, text);
        HugoPreferences.setWidgetCurrentStopName(context, text);
    }

    private void updateStopNameText(RemoteViews views, Context context){
        Log.d(TAG, "Stop Name called with no text");
        views.setTextViewText(R.id.widgetStopText, HugoPreferences.getWidgetCurrentStopName(context));
    }

    private void pushOutUpdate(RemoteViews views, Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, RealtimeWidgetProvider.class), views);
    }

    private void loadNextStop(RemoteViews views, Context context){

        Log.d(TAG, "Loading Next stop");

        Database db = new Database(context);
        ArrayList<Stop> stops = db.getFavouriteStops();

        if(stops == null || stops.size() == 0){
            Log.d(TAG, "No favourites set");
            showNoFavouritesSet(views);
            return;
        }

        if(getCurrentStopName(context).equalsIgnoreCase("")){
            Log.d(TAG, "Current stop name is empty");
            updateStopNameText(views, context, stops.get(0).getOverrideName());
            return;
        }

        if(stops.size() == 1){
            Log.d(TAG, "Getting predictions for single stop " + stops.get(0).getOverrideName());
            getPredictionsFromAPI(views, context, stops.get(0));
            return;
        }

        for(int i = 0; i < stops.size(); i++){
            Stop stop = stops.get(i);
            Log.d(TAG, "Looping through stop... found " + stop.getOverrideName());
            if(stop.getOverrideName().equalsIgnoreCase(getCurrentStopName(context))){
                //We should now populate the next one in the list
                if(i == (stops.size() - 1)){
                    Log.d(TAG, "Getting predictions for stop " + stops.get(0).getOverrideName());
                    updateStopNameText(views, context, stops.get(0).getOverrideName());
                    getPredictionsFromAPI(views, context, stops.get(0));
                    return;
                }else{
                    Log.d(TAG, "Getting predictions for stop " + stops.get(i + 1).getOverrideName());
                    updateStopNameText(views, context, stops.get(i + 1).getOverrideName());
                    getPredictionsFromAPI(views, context, stops.get(i + 1));
                    return;
                }
            }
        }

        updateStopNameText(views, context, stops.get(0).getOverrideName());
        getPredictionsFromAPI(views, context, stops.get(0));
    }

    private void loadPreviousStop(RemoteViews views, Context context){

        Database db = new Database(context);
        ArrayList<Stop> stops = db.getFavouriteStops();

        if(stops == null || stops.size() == 0){
            showNoFavouritesSet(views);
            return;
        }

        if(getCurrentStopName(context).equalsIgnoreCase("")){
            updateStopNameText(views, context, stops.get(0).getOverrideName());
            return;
        }

        if(stops.size() == 1){
            getPredictionsFromAPI(views, context, stops.get(0));
        }

        for(int i = 0; i < stops.size(); i++){
            Stop stop = stops.get(i);
            if(stop.getOverrideName().equalsIgnoreCase(getCurrentStopName(context))){
                //We should now populate the last one in the list
                if(i == 0){
                    updateStopNameText(views, context, stops.get(stops.size() - 1).getOverrideName());
                    getPredictionsFromAPI(views, context, stops.get(stops.size() - 1));
                    return;
                }else{
                    updateStopNameText(views, context, stops.get(i - 1).getOverrideName());
                    getPredictionsFromAPI(views, context, stops.get(i - 1));
                    return;
                }
            }
        }

        updateStopNameText(views, context, stops.get(0).getOverrideName());
        getPredictionsFromAPI(views, context, stops.get(0));
    }

    private void getPredictionsFromAPI(RemoteViews views, Context context){

        showRefreshing(views);

        Database db = new Database(context);
        ArrayList<Stop> stops = db.getFavouriteStops();

        if(stops == null || stops.size() == 0){
            showNoFavouritesSet(views);
        }else{

            if(HugoPreferences.getWidgetCurrentStopName(context).equalsIgnoreCase("")){
                updateStopNameText(views,context, stops.get(0).getOverrideName());
                getPredictionsFromAPI(views, context, stops.get(0));
            }else{
                for(Stop stop : stops){
                    if(stop.getOverrideName().equalsIgnoreCase(HugoPreferences.getWidgetCurrentStopName(context))){
                        getPredictionsFromAPI(views, context, stop);
                        return;
                    }
                }

                updateStopNameText(views, context, stops.get(0).getOverrideName());
                getPredictionsFromAPI(views, context, stops.get(0));
            }
        }
    }

    private void getPredictionsFromAPI(RemoteViews views, Context context, Stop stop) {

        //Here is where we want to set the data call off
        showRefreshing(views);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("atcoCode", stop.getAtcoCode());

            if(jobScheduler != null){
                jobScheduler.schedule(new JobInfo.Builder(1,
                        new ComponentName(context, RealtimeWidgetHttpService.class))
                        .setExtras(bundle)
                        .setMinimumLatency(1)
                        .setOverrideDeadline(1)
                        .build());
            }
        }
    }

    public static void showNoFavouritesSet(RemoteViews views){

        Log.d(TAG, "Showing no favourites set");
        views.setViewVisibility(R.id.widgetNoFavouritesView, View.VISIBLE);
        views.setViewVisibility(R.id.widgetRefreshingView, View.GONE);
        views.setViewVisibility(R.id.widgetNoDeparturesView, View.GONE);
        views.setViewVisibility(R.id.widgetNoNetworkView, View.GONE);

    }

    private void showNoDepartures(RemoteViews views){
        Log.d(TAG, "Showing no departures");
        views.setViewVisibility(R.id.widgetNoFavouritesView, View.GONE);
        views.setViewVisibility(R.id.widgetRefreshingView, View.GONE);
        views.setViewVisibility(R.id.widgetNoDeparturesView, View.VISIBLE);
        views.setViewVisibility(R.id.widgetNoNetworkView, View.GONE);
    }

    private void showDataScreenOnly(RemoteViews views){

        Log.d(TAG, "Showing data");
        views.setViewVisibility(R.id.widgetResultsList, View.VISIBLE);
        views.setViewVisibility(R.id.widgetNoFavouritesView, View.GONE);
        views.setViewVisibility(R.id.widgetRefreshingView, View.GONE);
        views.setViewVisibility(R.id.widgetNoDeparturesView, View.GONE);
        views.setViewVisibility(R.id.widgetNoNetworkView, View.GONE);
    }

    private void showRefreshing(RemoteViews views){

        Log.d(TAG, "Showing no departures");
        views.setViewVisibility(R.id.widgetNoFavouritesView, View.GONE);
        views.setViewVisibility(R.id.widgetRefreshingView, View.VISIBLE);
        views.setViewVisibility(R.id.widgetNoDeparturesView, View.GONE);
        views.setViewVisibility(R.id.widgetNoNetworkView, View.GONE);
    }

    private void showNoData(RemoteViews views) {
        Log.d(TAG, "Showing no data");
        views.setViewVisibility(R.id.widgetNoFavouritesView, View.GONE);
        views.setViewVisibility(R.id.widgetRefreshingView, View.GONE);
        views.setViewVisibility(R.id.widgetNoNetworkView, View.VISIBLE);
        views.setViewVisibility(R.id.widgetNoDeparturesView, View.GONE);
    }

    private String getCurrentStopName(Context context){
        String name = HugoPreferences.getWidgetCurrentStopName(context);
        Log.d(TAG, "Current stop name is " + name);
        return name;
    }

    private void updateListData(RemoteViews views, Context context){

        ArrayList<RealtimePrediction> predictions = HugoPreferences.getWidgetRealtimePredictions(context);
        if(predictions.size() == 0){
            Log.d(TAG, "No predictions to come back " + predictions);
            showNoDepartures(views);
            return;
        }

        showDataScreenOnly(views);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context, RealtimeWidgetProvider.class)),R.id.widgetResultsList);
        updateStopNameText(views,context);
    }

    private void updateLastUpdateTime(RemoteViews views) {
        String time  = "Last updated: " + LocalDateTime.now().toString("E dd MMM HH:mm");
        views.setTextViewText(R.id.widgetLastUpdatedText, time);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Data received back with action: " + intent.getAction());
        Log.d(TAG, "Widget Id: " + intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0));

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "widget");

        if(intent.getAction() == null){
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "no intent data");
            analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
            onEnabled(context);
        }else{
            if(intent.getAction() != null && intent.getAction().equals(ACTION_SHOW_DATA)){
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "show data");
                analytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, bundle);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
                updateListData(views, context);
                updateLastUpdateTime(views);
                pushOutUpdate(views, context);
            }else if(intent.getAction() != null && intent.getAction().equals(ACTION_NEXT_HIT)){
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "next button hit");
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                loadNextStop(views, context);
                pushOutUpdate(views, context);
            }else if(intent.getAction() != null && intent.getAction().equals(ACTION_NO_NETWORK)){
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "network unavailable");
                analytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, bundle);
                showNoData(views);
                pushOutUpdate(views, context);
            }else if(intent.getAction() != null && intent.getAction().equals(ACTION_BACK_HIT)){
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "back button hit");
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
                loadPreviousStop(views, context);
                pushOutUpdate(views, context);
            }else if(intent.getAction() != null && intent.getAction().equals(ACTION_REFRESH_HIT)){
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "refresh button hit");
                analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
                getPredictionsFromAPI(views, context);
                pushOutUpdate(views, context);
            }else{
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "unknown intent data");
                bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, intent.getAction());
                analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                super.onReceive(context, intent);
            }
        }

    }

}