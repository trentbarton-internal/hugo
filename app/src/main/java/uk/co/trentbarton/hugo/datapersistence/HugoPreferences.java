package uk.co.trentbarton.hugo.datapersistence;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import android.util.Log;

import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import uk.co.trentbarton.hugo.dataholders.Alarm;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DataRequestParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.ServiceFilter;
import uk.co.trentbarton.hugo.tools.HttpResponseParser;

public class HugoPreferences {

    private static final String PREFERENCE_FILE_NAME = "MAIN_PREFS";
    private static final String CURRENT_VERSION_PREF = "CurrentVersion";
    private static final String VERSION_INSTALLED = "HugoVersionInstalled";
    private static final String PUSH_SENT = "PushSent";
    private static final String PUSH_TOKEN = "PushToken";
    private static final String USER_TOKEN = "UserToken";
    private static final String FEEDBACK_SENT = "FeedBackSent";
    private static final String WIDGET_PREDICTIONS = "WidgetPredictions";
    private static final String WIDGET_CURRENT_STOP = "WidgetCurrentStop";
    private static final String STORED_FILTERS = "StoredFilters";
    private static final String NUMBER_OF_SERVICES_SUBSCRIBED = "SubscribedServices";
    private static final String ACTIVE_ALARM = "ActiveAlarm";
    private static final String FIRST_TIME_OPENED = "FirstTimeOpened";
    private static final String ALIGHTING_ALARM_NAME = "AlightingAlarmName";
    private static final String LAST_JOURNEY_DATA = "LastJourneyData";
    private static final String LAST_JOURNEY_ITEM_CHOSEN = "LastJourneyItemChosen";
    private static final String MESSAGES_SUBSCRIBED_TO = "MessagesSubscribedTo";
    private static final String ALERT_STATUS = "AlertStatus";
    private static final String TAG = HugoPreferences.class.getSimpleName();

    public static void resetAll(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //Get rid of old values that we no longer need

        setCurrentStopsVersion(context,1500);
        setHugoVersionInstalled(context, 0);
        editor.remove("FirstInstall");
        editor.remove("AlarmSetFor");
        editor.remove("FeedbackSent");
        editor.remove("Syncing");
        editor.remove("Updated");
    }

    public static void setCurrentStopsVersion(Context context, int version){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CURRENT_VERSION_PREF, version);
        editor.apply();
    }

    public static int getCurrentStopsVersion(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getInt(CURRENT_VERSION_PREF, 1500);
    }

    public static int getHugoVersionInstalled(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getInt(VERSION_INSTALLED, 0);
    }

    public static void setHugoVersionInstalled(Context context, int version){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(VERSION_INSTALLED, version);
        editor.apply();
    }

    public static int getNumberOfServicesSubscribedTo(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getInt(NUMBER_OF_SERVICES_SUBSCRIBED, 0);
    }

    public static void setNumberOfServicesSubscribed(Context context, int numOfServices){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(NUMBER_OF_SERVICES_SUBSCRIBED, numOfServices);
        editor.apply();
    }

    public static String getUserToken(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(USER_TOKEN, "");
    }

    public static void setUserToken(Context context, String token){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_TOKEN, token);
        editor.commit();
    }

    public static Alarm getActiveAlarm(Context context){

        try{
            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
            String jsonString = sharedPref.getString(ACTIVE_ALARM, "");
            if(jsonString.equalsIgnoreCase("")){
                return null;
            }

            Alarm alarm = new Alarm(jsonString);
            if(alarm.getScheduledTime().plusMinutes(60).isBefore(LocalDateTime.now())){
                setActiveAlarm(context, null);
                return null;
            }else{
                return alarm;
            }
        }catch(Exception ex){
            return null;
        }
    }

    public static void setActiveAlarm(Context context, Alarm alarm){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        try{
            editor.putString(ACTIVE_ALARM, alarm.toJsonString());
        }catch(Exception e){
            editor.putString(ACTIVE_ALARM, "");
        }
        editor.apply();
    }

    public static boolean isPushSent(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getBoolean(PUSH_SENT, false);
    }

    public static void setPushSent(Context context, boolean value){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PUSH_SENT, value);
        editor.commit();
    }

    public static void setPushToken(Context context, String pushToken){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PUSH_TOKEN, pushToken);
        editor.commit();

    }

    public static String getPushToken(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getString(PUSH_TOKEN, "");
    }

    public static void setFeedbackSent(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(FEEDBACK_SENT, System.currentTimeMillis());
        editor.commit();
    }

    public static boolean canSendFeedback(Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        long lastTimeSent = sharedPref.getLong(FEEDBACK_SENT, 0);
        return System.currentTimeMillis() - lastTimeSent > (1000 * 60 * 5); //No feedback can be sent again within 5 minutes

    }

    public static boolean setWidgetRealtimePredictions(Context context, String atcoCode, ArrayList<RealtimePrediction> predictions){

        if(predictions == null || predictions.size() == 0){
            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(WIDGET_PREDICTIONS, "");
            editor.commit();
        }

        try{
            JSONObject object = new JSONObject();
            object.put("atcoCode", atcoCode);
            JSONArray predictionsArray = new JSONArray();

            for(RealtimePrediction prediction : predictions){
                predictionsArray.put(prediction.toJsonString());
            }

            object.put("predictions", predictionsArray);

            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(WIDGET_PREDICTIONS, object.toString());
            editor.commit();

            return true;
        }catch(Exception e){
            return false;
        }

    }

    public static ArrayList<RealtimePrediction> getWidgetRealtimePredictions(Context context){

        Log.i(TAG, "getWidgetPredictions called");

        try{

            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            String jsonString = sharedPref.getString(WIDGET_PREDICTIONS, "");

            if(jsonString.equalsIgnoreCase("")){
                Log.i(TAG, "getWidgetPredictions were empty");
                return new ArrayList<>();
            }

            JSONObject object = new JSONObject(jsonString);
            JSONArray predictionsArray = object.getJSONArray("predictions");
            ArrayList<RealtimePrediction> predictionsToSendBack = new ArrayList<>();

            for(int i = 0; i < predictionsArray.length(); i++){
                Object predictionsObject = predictionsArray.get(i);
                String objectString = predictionsObject.toString();
                RealtimePrediction prediction = new RealtimePrediction(new JSONObject(objectString));
                predictionsToSendBack.add(prediction);
            }

            return predictionsToSendBack;


        }catch(Exception e){
            Log.e(TAG, e.getLocalizedMessage(), e);
            return new ArrayList<>();
        }

    }

    public static String getWidgetCurrentStopName(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(WIDGET_CURRENT_STOP, "");
    }

    public static void setWidgetCurrentStopName(Context context, String token){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(WIDGET_CURRENT_STOP, token);
        editor.apply();
    }

    public static void saveStopFilter(Context context, String atcoCode, ServiceFilter filter){

        //Get the JsonObject stored already
        Map<String, ServiceFilter> filters = getStopFilters(context);
        filters.put(atcoCode, filter);
        storeStopFilters(context, filters);

    }

    public static void removeStopFilter(Context context, String atcoCode){
        Map<String, ServiceFilter> filters = getStopFilters(context);
        filters.remove(atcoCode);
        storeStopFilters(context, filters);
    }

    @Nullable
    public static ServiceFilter getFilterForStop(Context context, String atcoCode){
        return getStopFilters(context).get(atcoCode);
    }

    private static void storeStopFilters(Context context, Map<String, ServiceFilter> filtersMap){

        if(filtersMap.isEmpty()){
            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(STORED_FILTERS, "");
            editor.commit();
            return;
        }

        try{

            JSONObject object = new JSONObject();
            JSONArray stopsArray = new JSONArray();

            for(String key: filtersMap.keySet()){

                JSONObject stopObject = new JSONObject();
                stopObject.put("atcoCode", key);
                JSONArray filters = new JSONArray();

                ServiceFilter filter = filtersMap.get(key);
                String[] filterNames = filter.getAllServiceNames();
                for(int i = 0; i < filterNames.length ; i++){
                    filters.put(filterNames[i]);
                }
                stopObject.put("filters",filters);
                stopsArray.put(stopObject);
            }

            object.put("data", stopsArray);

            String jsonString = object.toString();

            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(STORED_FILTERS, jsonString);
            editor.commit();

            Log.i(TAG, "Filters stored successfully");

        }catch(JSONException ignore){
            Log.e(TAG, "Could not convert saveStopFilters into JSON Object", ignore);
        }catch(Exception e){
            Log.e(TAG, "Unknown error when saving filters", e);
        }

    }


    private static Map<String, ServiceFilter> getStopFilters(Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(STORED_FILTERS, "");

        if(jsonString.equalsIgnoreCase("")){
            return new HashMap<>();
        }
        try{
            Map<String, ServiceFilter> myFilters = new HashMap<>();
            JSONObject object = new JSONObject(jsonString);
            JSONArray objectArray = object.getJSONArray("data");

            for(int i = 0; i < objectArray.length(); i++){

                JSONObject stopObject = objectArray.getJSONObject(i);
                String atcoCode = stopObject.getString("atcoCode");
                ServiceFilter filter = new ServiceFilter();
                JSONArray filterNames = stopObject.getJSONArray("filters");

                for(int x = 0; x < filterNames.length(); x++){
                    filter.addServiceName((String)filterNames.get(x));
                }

                myFilters.put(atcoCode, filter);
            }

            return myFilters;

        }catch(JSONException ignore){
            Log.e(TAG, "Could not convert getStopFilters into JSON Object", ignore);
            return new HashMap<>();
        }

    }

    public static boolean isFirstOpening(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getBoolean(FIRST_TIME_OPENED, true);
    }

    public static void setAppOpened(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_TIME_OPENED, false);
        editor.apply();
    }

    public static String getAlightingAlarmStopName(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getString(ALIGHTING_ALARM_NAME, "Unknown");
    }

    public static void setAlightingAlarmStopName(Context context, String name){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ALIGHTING_ALARM_NAME, name);
        editor.apply();
    }

    public static void setLastJourneyData(Context context, String json) {

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_JOURNEY_DATA, json);
        editor.apply();

    }

    public static ArrayList<Journey> getLastJourneyData(Context context){

        int lastItemChosen = getLastJourneyItemChosen(context);

        if(lastItemChosen == -1){
            return null;
        }

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        String response = sharedPref.getString(LAST_JOURNEY_DATA, "");

        if(response.equalsIgnoreCase("")){
            return null;
        }

        try{
            HttpResponseParser parser = new HttpResponseParser(DataRequestParams.ApiCalls.GET_JOURNEY, response);
            ArrayList<Journey> obj = (ArrayList<Journey>) parser.getResponseObject();

            long currentTime = System.currentTimeMillis() / 1000;

            if(obj.get(0).getArrivalTime() <= currentTime){
                setLastJourneyData(context, "");
                return null;
            }

            return obj;
        }catch(Exception e){
            return null;
        }
    }

    public static int getLastJourneyItemChosen(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getInt(LAST_JOURNEY_ITEM_CHOSEN, -1);
    }

    public static void setLastJourneyItemChosen(Context context, int journeyChosen){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(LAST_JOURNEY_ITEM_CHOSEN, journeyChosen);
        editor.apply();
    }

    public static boolean userSubscribed(Context context, String name) {

        if(name.equalsIgnoreCase("test") || name.equalsIgnoreCase("direct")){
            return true;
        }

        try{
            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
            String result = sharedPref.getString(MESSAGES_SUBSCRIBED_TO, "empty");

            if(result.equalsIgnoreCase("empty")){
                return true;
            }

            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONArray("messageArray");

            for(int i=0; i < arr.length(); i++){
                String typeStored = arr.getString(i);
                if(typeStored.equalsIgnoreCase(name)){
                    return true;
                }
            }

            return false;

        }catch(Exception e){
            return true;
        }

    }

    public static void setMessageTypesSubscribedTo(Context context, ArrayList<String> strings){

        if(context == null){
            return;
        }

        String result = "";

        try{
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            for(String s : strings){
                arr.put(s);
            }
            obj.put("messageArray", arr);
            result = obj.toString();
        }catch(Exception e){
            //Do nothing
        }

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MESSAGES_SUBSCRIBED_TO, result);
        editor.apply();
    }

    public static String getAlertStatus(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPref.getString(ALERT_STATUS, "");
    }

    public static void saveAlertStatus(Context context, String s) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ALERT_STATUS, s);
        editor.apply();
    }
}
