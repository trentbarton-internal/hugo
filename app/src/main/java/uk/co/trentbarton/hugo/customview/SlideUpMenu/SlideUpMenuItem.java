package uk.co.trentbarton.hugo.customview.SlideUpMenu;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class SlideUpMenuItem{

    protected int mPosition;
    protected String mTitle;
    protected int mTextColour;
    protected boolean isBottom = false;
    protected RelativeLayout mView;
    protected TextView mTitleText;
    protected LinearLayout mBottomBorder;
    protected Context mContext;
    protected View.OnClickListener mListener;

    public SlideUpMenuItem(Context context, String title) {
        mTitle = title;
        mContext = context;
        mTextColour = Color.BLACK;
        createView();
    }

    public SlideUpMenuItem(Context context, String title, int textColour){
        mTitle = title;
        mContext = context;
        mTextColour = textColour;
        createView();
    }

    protected void createView() {

        RelativeLayout layout = new RelativeLayout(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(61));
        layout.setLayoutParams(params);

        mTitleText = new TextView(mContext);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(60));
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mTitleText.setText(mTitle);
        mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mTitleText.setTextColor(mTextColour);
        mTitleText.setGravity(Gravity.CENTER);
        mTitleText.setLayoutParams(titleParams);

        layout.addView(mTitleText);

        mBottomBorder = new LinearLayout(mContext);
        RelativeLayout.LayoutParams borderParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(1));
        borderParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBottomBorder.setLayoutParams(borderParams);
        mBottomBorder.setBackgroundColor(mContext.getResources().getColor(R.color.background_dark_grey));

        layout.addView(mBottomBorder);

        mView = layout;

        mView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onClick(v);
            }
        });

    }

    public void setOnClickListener(View.OnClickListener l){
        mListener = l;
    }

    public RelativeLayout getView(){
        return this.mView;
    }

    public void setAsLastItem(){
        this.isBottom = true;
        mBottomBorder.setVisibility(View.GONE);
    }

    public void unsetAsLastItem(){
        this.isBottom = false;
        mBottomBorder.setVisibility(View.VISIBLE);
    }

    public void assignPosition(int pos){
        this.mPosition = pos;
    }

    public int getPosition(){
        return this.mPosition;
    }


}
