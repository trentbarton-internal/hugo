package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class SendPushTokenParams extends DataRequestParams {

    private boolean tokenAssigned = false;

    public SendPushTokenParams(Context context) {
        super(ApiCalls.REGISTER_PUSH_TOKEN, context);
    }

    public void setPushToken(String token){
        try{
            add("push_token",token);
            tokenAssigned = true;
        }catch(Exception ignore){}
    }

    @Override
    public boolean validate() {
        return super.validate() && tokenAssigned;
    }
}
