package uk.co.trentbarton.hugo.dataholders;

import java.util.ArrayList;

public class UserDetails {

    private final ArrayList<Message> mUserMessages;
    private final ArrayList<Message> mTrafficAlerts;
    private final ArrayList<Alarm> mUserAlarms;
    private int mUserID;
    private String mPushToken;

    public UserDetails(){
        mUserMessages = new ArrayList<>();
        mTrafficAlerts = new ArrayList<>();
        mUserAlarms = new ArrayList<>();
    }

    public ArrayList<Message> getUserMessages() {
        return new ArrayList<>(mUserMessages);
    }

    public ArrayList<Message> getTrafficAlerts() {
        return new ArrayList<>(mTrafficAlerts);
    }

    public ArrayList<Alarm> getUserAlarms() {
        return new ArrayList<>(mUserAlarms);
    }

    public int getUserID(){
        return mUserID;
    }

    public void setUserID(int userID){
        mUserID = userID;
    }

    public void addMessage(Message message){
        mUserMessages.add(message);
    }

    public void addAlert(Message disruption){
        mTrafficAlerts.add(disruption);
    }

    public void addAlarm(Alarm alarm){
        mUserAlarms.add(alarm);
    }

    public String getPushToken() {
        return mPushToken;
    }

    public void setPushToken(String mPushToken) {
        this.mPushToken = mPushToken;
    }
}
