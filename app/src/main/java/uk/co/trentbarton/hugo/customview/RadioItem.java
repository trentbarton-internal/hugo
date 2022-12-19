package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tools.Metrics;

public class RadioItem extends LinearLayout{

    private String title;
    private boolean isSelected;
    private TextView mTitleTextView;
    private ImageView mImageView;

    public RadioItem(Context context) {
        super(context);
        init();
    }

    public RadioItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadioItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RadioItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {

        setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(36));
        setPadding(Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8));
        setLayoutParams(params);
        mTitleTextView = new TextView(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        textParams.weight = 1;
        mTitleTextView.setLayoutParams(textParams);
        addView(mTitleTextView);
        mTitleTextView.setGravity(Gravity.CENTER_VERTICAL);
        mImageView = new ImageView(getContext());
        LinearLayout.LayoutParams mImageParams = new LinearLayout.LayoutParams(Metrics.densityPixelsToPixels(20), Metrics.densityPixelsToPixels(20));
        mImageView.setLayoutParams(mImageParams);
        mImageView.setBackgroundResource(R.drawable.unselected_icon);
        addView(mImageView);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        mTitleTextView.setText(title);
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
        changeIcon();
    }

    private void changeIcon(){
        if(isSelected){
            mImageView.setBackgroundResource(R.drawable.green_tick);
        }else{
            mImageView.setBackgroundResource(R.drawable.unselected_icon);
        }
    }

    public void toggle(){
        setSelected(!isSelected);
        changeIcon();
    }
}
