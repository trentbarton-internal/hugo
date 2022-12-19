package uk.co.trentbarton.hugo.customview.SlideUpMenu;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.tools.Tools;

public class SlideUpTextItem extends SlideUpMenuItem {

    private String mTextMessage;

    public SlideUpTextItem(Context context, String title) {
        super(context, title);
    }

    public SlideUpTextItem(Context context, String title, int textColour) {
        super(context, title, textColour);
    }

    public SlideUpTextItem(Context context, String title, int textColour, String textMessage){
        super(context, title, textColour);
        mTextMessage = textMessage;
        alterView();
    }

    private void alterView(){

        //Rest the view's height to be 121dp high not 61dp
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(122));
        mView.setLayoutParams(params);

        //Add the new titleText tot he top of the view
        TextView infoText = new TextView(mContext);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(60));
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        infoText.setPadding(Metrics.densityPixelsToPixels(15),0,Metrics.densityPixelsToPixels(15),0);
        infoText.setText(mTextMessage);
        infoText.setId(Tools.generateViewId());
        infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        infoText.setTextColor(mContext.getResources().getColor(R.color.background_dark_grey));
        infoText.setGravity(Gravity.CENTER);
        infoText.setLayoutParams(titleParams);

        mView.addView(infoText);

        //Add a middle border
        LinearLayout middleBorder = new LinearLayout(mContext);
        RelativeLayout.LayoutParams borderParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(1));
        borderParams.addRule(RelativeLayout.BELOW, infoText.getId());
        middleBorder.setLayoutParams(borderParams);
        middleBorder.setBackgroundColor(mContext.getResources().getColor(R.color.background_light_grey));

        mView.addView(middleBorder);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(60));
        textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mTitleText.setLayoutParams(textParams);
        mTitleText.setGravity(Gravity.CENTER);


    }



}
