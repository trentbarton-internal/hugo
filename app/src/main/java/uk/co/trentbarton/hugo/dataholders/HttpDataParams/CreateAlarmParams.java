package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import uk.co.trentbarton.hugo.dataholders.Alarm;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public class CreateAlarmParams extends DataRequestParams{

    private Alarm mAlarm;

    public CreateAlarmParams(Context context) {
        super(ApiCalls.CREATE_ALARM, context);
    }

    public void addAlarm(Alarm alarm){

        mAlarm = alarm;
        try{
            add("push_token", HugoPreferences.getPushToken(mContext));
            add("atco_code", alarm.getAtcoCode());
            add("service_name", alarm.getServiceName());
            add("departure_time", alarm.getScheduledTime().toDate().getTime());
            add("minimum_time", alarm.getMinuteTrigger());

        }catch(Exception e){
            mAlarm = null;
        }

    }

    @Override
    public boolean validate() {
        return super.validate() && mAlarm != null;
    }





}
