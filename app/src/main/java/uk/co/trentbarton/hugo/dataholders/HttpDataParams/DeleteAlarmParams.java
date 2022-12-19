package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;
import android.util.Log;

import uk.co.trentbarton.hugo.dataholders.Alarm;

public class DeleteAlarmParams extends DataRequestParams {

    Alarm mAlarm;
    private final String TAG = this.getClass().getSimpleName();

    public DeleteAlarmParams(Context context) {
        super(ApiCalls.DELETE_ALARM, context);
    }

    public void addAlarm(Alarm alarm){
        try{
            add("alarm_id", alarm.getAlarmID());
            mAlarm = alarm;
        }catch(Exception e){
            Log.d(TAG, "adding the alarm failed - " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean validate() {
        return super.validate() && mAlarm != null;
    }
}
