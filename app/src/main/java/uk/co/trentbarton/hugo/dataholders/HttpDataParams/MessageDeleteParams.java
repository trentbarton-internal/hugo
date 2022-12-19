package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import uk.co.trentbarton.hugo.dataholders.Message;

public class MessageDeleteParams extends DataRequestParams {

    private Message mMessage;

    public MessageDeleteParams(Context context) {
        super(ApiCalls.DELETE_MESSAGE, context);
    }

    public MessageDeleteParams addMessage(Message message){

        try{
            add("message_id", message.getMessageId());
            mMessage = message;
        }catch(Exception ignore){}

        return this;

    }

    @Override
    public boolean validate() {
        return super.validate() && (mMessage != null);
    }
}
