package uk.co.trentbarton.hugo.dataholders;

import android.util.JsonReader;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class Alarm {

    private LocalDateTime scheduledTime;
    private String atcoCode;
    private String stopName;
    private String serviceName;
    private int alarmID;
    private int minuteTrigger;

    public Alarm(){

    }

    public Alarm(String jsonString) throws Exception{

        JSONObject obj = new JSONObject(jsonString);
        this.scheduledTime = LocalDateTime.parse(obj.getString("scheduledTime"));
        this.atcoCode = obj.getString("atcoCode");
        this.serviceName = obj.getString("serviceName");
        this.alarmID = obj.getInt("alarmID");
        this.minuteTrigger = obj.getInt("minuteTrigger");
        this.stopName = obj.getString("stopName");

    }

    public String toJsonString() throws Exception{

        JSONObject obj = new JSONObject();
        obj.put("scheduledTime", scheduledTime.toString());
        obj.put("atcoCode", atcoCode);
        obj.put("serviceName", serviceName);
        obj.put("alarmID", alarmID);
        obj.put("minuteTrigger", minuteTrigger);
        obj.put("stopName", stopName);
        return obj.toString();

    }

    public int getMinuteTrigger() {
        return minuteTrigger;
    }

    public void setMinuteTrigger(int minuteTrigger) {
        this.minuteTrigger = minuteTrigger;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getAtcoCode() {
        return atcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        this.atcoCode = atcoCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getAlarmID() {
        return alarmID;
    }

    public void setAlarmID(int alarmID) {
        this.alarmID = alarmID;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }
}
