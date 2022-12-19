package uk.co.trentbarton.hugo.customview.SlideUpMenu;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.tools.Tools;
import uk.co.trentbarton.hugo.R;

public class SlideUpServiceItem extends SlideUpMenuItem {

    private int mServiceColour;
    private boolean mSelected = false;
    private ImageView mSwitchImage;

    public SlideUpServiceItem(Context context, String title) {
        super(context, title);
    }

    public SlideUpServiceItem(Context context, String title, int textColour, int serviceColour) {
        super(context, title, textColour);
        mServiceColour = serviceColour;
        alterView();
    }

    private void alterView(){

        mView.setOnClickListener(v -> {
            changeSelection();
        });

        ImageView imv = new ImageView(mContext);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Metrics.densityPixelsToPixels(40), Metrics.densityPixelsToPixels(40));
        layoutParams.setMargins(Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10));
        imv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imv.setId(Tools.generateViewId());
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        imv.setLayoutParams(layoutParams);
        imv.setImageResource(R.drawable.circle_cover);
        imv.setBackgroundColor(mServiceColour);

        mView.addView(imv);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(60));
        textParams.addRule(RelativeLayout.RIGHT_OF, imv.getId());
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mTitleText.setLayoutParams(textParams);
        mTitleText.setGravity(Gravity.CENTER);

        mSwitchImage = new ImageView(mContext);
        RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(Metrics.densityPixelsToPixels(40), Metrics.densityPixelsToPixels(40));
        switchParams.setMargins(Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10));
        mSwitchImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mSwitchImage.setId(Tools.generateViewId());
        switchParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        switchParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mSwitchImage.setLayoutParams(switchParams);

        mView.addView(mSwitchImage);
        changeSelection();

    }

    private void changeSelection() {

        this.mSelected = !this.mSelected;

        if(mSelected){
            mSwitchImage.setImageResource(R.drawable.green_tick);
        }else{
            mSwitchImage.setImageResource(R.drawable.unselected_icon);
        }

        mSwitchImage.invalidate();

    }

    public void forceToSelected(){
        if(!isSelected()) changeSelection();
    }

    public boolean isSelected(){
        return this.mSelected;
    }

    public String getServiceName(){
        return mTitle;
    }


    public void unselect() {
        if(isSelected()){
            changeSelection();
        }
    }
}
