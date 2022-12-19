package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class SendFeedbackParams extends DataRequestParams {

    public SendFeedbackParams(Context context) {
        super(ApiCalls.SEND_FEEDBACK, context);
    }

    public SendFeedbackParams setLookAndFeelScore(int score){
        try{
            add("look_and_feel",score);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setEaseOfUseScore(int score){
        try{
            add("ease_of_use",score);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setFeaturesScore(int score){
        try{
            add("features",score);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setImproveComments(String comments){
        try{
            add("improve_comments",comments);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setBitsLiked(String comments){
        try{
            add("bits_liked",comments);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setBitsDisliked(String comments){
        try{
            add("bits_disliked",comments);
        }catch(Exception ignore){}

        return this;
    }

    public SendFeedbackParams setFrequencyOfUse(String comments){
        try{
            add("how_often_is_use",comments);
        }catch(Exception ignore){}

        return this;
    }


}
