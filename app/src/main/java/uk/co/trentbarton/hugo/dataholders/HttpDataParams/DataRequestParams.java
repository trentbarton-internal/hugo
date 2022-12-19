package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public abstract class DataRequestParams {

    private static final String API_KEY = "9gs9c4ifoc2en5ipea9w";
    private JSONObject mParams;
    private boolean createdSuccessfully = false;
    private ApiCalls mCall;
    protected Context mContext;
    private String TAG = this.getClass().getSimpleName();

    protected DataRequestParams(ApiCalls call, Context context){
        mParams = new JSONObject();
        mContext = context;
        init(call, context);
    }

    public enum ApiCalls{
        REGISTER_USER("register_user"),
        GET_JOURNEY("get_journey"),
        GET_REALTIME_FULL("get_realtime_full"),
        GET_REALTIME_BASIC("get_realtime_basic"),
        GET_REALTIME_IN_AREA("get_realtime_in_area_full"),
        GET_REALTIME_IN_AREA_BASIC("get_realtime_in_area_basic"),
        GET_MULTIPLE_REALTIME_FULL("get_realtime_full"),
        GET_STOPS("get_stops"),
        REGISTER_PUSH_TOKEN("assign_push_token"),
        GET_WEATHER("get_weather"),
        CREATE_ALARM("create_alarm"),
        DELETE_ALARM("delete_alarm"),
        GET_SERVICES("get_services"),
        DELETE_MESSAGE("delete_message"),
        MESSAGE_READ("message_read"),
        GET_ALL_USER_DETAILS("get_all_user_details"),
        SEND_FEEDBACK("send_feedback"),
        SET_USER_PREFERENCES("set_all_user_preferences"),
        GET_PLACES("get_places");

        String functionName;

        String getFunctionName(){
            return this.functionName;
        }

        ApiCalls(String s){
            functionName = s;
        }

    }

    private void init(ApiCalls call, Context context){

        mCall = call;

        try{
            add("apiKey", API_KEY);
            add("function", call.getFunctionName());
            add("token", HugoPreferences.getUserToken(context));
            createdSuccessfully = true;
        }catch(Exception ignored){

        }

    }

    protected DataRequestParams add(String key, double value) throws Exception{

        if(mParams.has(key)){
            mParams.remove(key);
        }
        mParams.put(key, value);
        return this;
    }

    protected DataRequestParams add(String key, String[] value) throws Exception{

        if(mParams.has(key)){
            mParams.remove(key);
        }

        JSONArray arr = new JSONArray();
        for(int i = 0; i < value.length; i++){
            arr.put(value[i]);
        }

        mParams.put(key, arr);
        return this;
    }

    protected DataRequestParams add(String key, int value) throws Exception{
        if(mParams.has(key)){
            mParams.remove(key);
        }
        mParams.put(key, value);
        return this;
    }

    protected DataRequestParams add(String key, String value) throws Exception{
        if(mParams.has(key)){
            mParams.remove(key);
        }
        mParams.put(key, value);
        return this;
    }

    protected DataRequestParams add(String key, boolean value) throws Exception{
        if(mParams.has(key)){
            mParams.remove(key);
        }
        mParams.put(key, value);
        return this;
    }

    protected DataRequestParams add(String key, long value) throws Exception{
        if(mParams.has(key)){
            mParams.remove(key);
        }
        mParams.put(key, value);
        return this;
    }

    protected DataRequestParams remove(String key){
        if(mParams.has(key)){
            mParams.remove(key);
        }
        return this;
    }

    private String getJsonString(){
        return this.mParams.toString();
    }

    public String build(){

        try {
            add("token", HugoPreferences.getUserToken(mContext));
        }catch (Exception e){
            Log.e(TAG, "Couldn't set token in request params");
            Log.e(TAG, e.getLocalizedMessage());
            createdSuccessfully = false;
        }

        if(validate()){
            return getJsonString();
        }else{
            throw new RuntimeException("The Params do not pass validation, make sure you call validate first before trying to build");
        }
    };

    public boolean validate(){
        return this.createdSuccessfully;
    };

    public ApiCalls getCall(){ return this.mCall;}

}
