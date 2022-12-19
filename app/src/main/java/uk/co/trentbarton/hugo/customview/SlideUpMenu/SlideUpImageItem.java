package uk.co.trentbarton.hugo.customview.SlideUpMenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.tools.Tools;

public class SlideUpImageItem extends SlideUpMenuItem {

    int mImageResource = -1;
    Bitmap mBitmap = null;

    public SlideUpImageItem(Context context, String title, Bitmap bitmap) {
        super(context, title);
        mBitmap = bitmap;
        alterView();
    }

    public SlideUpImageItem(Context context, String title, int bitmapResource) {
        super(context, title);
        mImageResource = bitmapResource;
        alterView();
    }

    public SlideUpImageItem(Context context, String title, Bitmap bitmap, int textColour) {
        super(context, title, textColour);
        mBitmap = bitmap;
        alterView();
    }

    public SlideUpImageItem(Context context, String title, int bitmapResource, int textColour){
        super(context, title, textColour);
        mImageResource = bitmapResource;
        alterView();
    }

    private void alterView(){

        ImageView imv = new ImageView(mContext);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Metrics.densityPixelsToPixels(60), Metrics.densityPixelsToPixels(60));
        imv.setPadding(Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10));
        imv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imv.setId(Tools.generateViewId());
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        imv.setLayoutParams(layoutParams);

        if(mBitmap == null){
            imv.setImageResource(mImageResource);
        }else{
            imv.setImageBitmap(mBitmap);
        }

        mView.addView(imv);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(60));
        textParams.addRule(RelativeLayout.RIGHT_OF, imv.getId());
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mTitleText.setLayoutParams(textParams);
        mTitleText.setGravity(Gravity.CENTER);

    }


}
