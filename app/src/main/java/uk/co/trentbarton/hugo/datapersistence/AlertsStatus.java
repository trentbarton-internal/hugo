package uk.co.trentbarton.hugo.datapersistence;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlertsStatus {

    Map<Integer, Alert> alerts;
    private final Context mContext;
    private static AlertsStatus mInstance;

    public static AlertsStatus getInstance(Context context) {

        if(mInstance == null){
            mInstance = new AlertsStatus(context);
        }

        return mInstance;
    }

    private AlertsStatus(Context context){
        alerts = new HashMap<>();
        mContext = context;

        try{
            loadFromPreferences();
        }catch(Exception ignore){
        }
    }

    private void loadFromPreferences() throws Exception{

        String result = HugoPreferences.getAlertStatus(mContext);
        JSONObject obj = new JSONObject(result);
        JSONArray arr = obj.getJSONArray("values");

        for(int i = 0; i < arr.length(); i++){
            JSONObject alertObj = arr.getJSONObject(i);
            int id = alertObj.getInt("id");
            Alert a = new Alert(id);
            a.opened = alertObj.getBoolean("opened");
            a.deleted = alertObj.getBoolean("deleted");
            alerts.put(id, a);
        }


    }

    public boolean addNewAlert(int id){
        if(!alerts.containsKey(id)){
            alerts.put(id, new Alert(id));
            return true;
        }
        return false;
    }

    public void registerAlertOpened(int id){
        if(alerts.containsKey(id)){
            alerts.get(id).opened = true;
        }
    }

    public void registerAlertDeleted(int id){
        if(alerts.containsKey(id)){
            alerts.get(id).deleted = true;
        }
    }

    public boolean saveDetails(){

        try{
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();

            for(int id : alerts.keySet()){

                Alert a = alerts.get(id);
                JSONObject alertObj = new JSONObject();
                alertObj.put("id", a.alertId);
                alertObj.put("deleted", a.deleted);
                alertObj.put("opened", a.opened);
                arr.put(alertObj);

            }

            obj.put("values", arr);
            HugoPreferences.saveAlertStatus(mContext, obj.toString());
            return true;
        }catch(Exception e){
            return false;
        }



    }

    public boolean isAlertDeleted(int messageId) {

        Alert alert = alerts.get(messageId);

        if(alert == null){
            return false;
        }else{
            return alert.deleted;
        }
    }

    public boolean isAlertOpened(int messageId) {

        Alert alert = alerts.get(messageId);

        if(alert == null){
            return false;
        }else{
            return alert.opened;
        }
    }

    private class Alert{
        private final int alertId;
        private boolean deleted, opened;

        Alert(int id){
            alertId = id;
            deleted = false;
            opened = false;
        }
    }

}
