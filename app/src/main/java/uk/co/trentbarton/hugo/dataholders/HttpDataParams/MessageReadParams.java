package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import uk.co.trentbarton.hugo.dataholders.Message;

public class MessageReadParams extends DataRequestParams {

    private Message mMessage;

    public MessageReadParams(Context context) {
        super(ApiCalls.MESSAGE_READ, context);
    }

    public MessageReadParams addMessage(Message message){

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
