package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public class PushTokenParams extends DataRequestParams {

    private boolean addedPushToken = false;

    public PushTokenParams(Context context) {
        super(ApiCalls.REGISTER_PUSH_TOKEN, context);
        addPushToken(context);
    }


    @Override
    public boolean validate() {
        return super.validate() && this.addedPushToken;
    }

    private void addPushToken(Context context){

        try{
            add("push_token", HugoPreferences.getPushToken(context));
            addedPushToken = true;
        }catch(Exception ignore){}

    }
}
