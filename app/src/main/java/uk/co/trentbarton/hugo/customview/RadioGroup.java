package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.trentbarton.hugo.interfaces.RadioItemClickedListener;
import uk.co.trentbarton.hugo.tools.Metrics;

public class RadioGroup extends LinearLayout implements View.OnClickListener{

    private List<RadioItem> mItems;
    private String mTitle;
    private String mDescription;
    private TextView mTitleTextView, mDescriptionTextView;
    private RadioItemClickedListener mItemClickedListener;

    public RadioGroup(Context context) {
        super(context);
        init();
    }

    public RadioGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadioGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RadioGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mItems = new ArrayList<>();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);
        this.setLayoutParams(mParams);

        mTitleTextView = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTitleTextView.setTextColor(Color.BLACK);
        mTitleTextView.setTypeface(mTitleTextView.getTypeface(), Typeface.BOLD);
        mTitleTextView.setPadding(Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8));
        mTitleTextView.setLayoutParams(titleParams);
        addView(mTitleTextView);

        mDescriptionTextView = new TextView(getContext());
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mDescriptionTextView.setTextColor(Color.BLACK);
        mDescriptionTextView.setMaxLines(10);
        mDescriptionTextView.setGravity(Gravity.CENTER_VERTICAL);
        mDescriptionTextView.setPadding(Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8),Metrics.densityPixelsToPixels(8));
        mDescriptionTextView.setLayoutParams(descriptionParams);
        addView(mDescriptionTextView);

    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
        mTitleTextView.setText(mTitle);
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
        mDescriptionTextView.setText(mDescription);
    }

    public void addItem(String title){
        RadioItem item = new RadioItem(getContext());
        item.setTitle(title);
        mItems.add(item);
        item.setOnClickListener(this);
        if(mItems.size() == 1){
            item.setSelected(true);
        }
        this.addView(item);
    }

    public void setOnItemClickedListener(RadioItemClickedListener listener){
        mItemClickedListener = listener;
    }

    @Nullable
    public RadioItem getCheckedItem(){

        for(RadioItem item : mItems){
            if(item.isSelected()){
                return item;
            }
        }

        return null;

    }

    public void setCheckedItem(RadioItem item){
        for(RadioItem radioItem : mItems){
            if(item == radioItem){
                radioItem.setSelected(true);
                break;
            }
        }
    }

    public void setCheckedItem(int position){

        if(position < 0 || position > (mItems.size() - 1)){
            //Out of bounds exception
            return;
        }else{
            mItems.get(position).setSelected(true);
        }

    }

    public int getSize(){
        return mItems.size();
    }


    @Override
    public void onClick(View v) {
        RadioItem tapped = ((RadioItem) v);
        for(RadioItem item : mItems){
            if(item == tapped){
                item.setSelected(true);
            }else{
                item.setSelected(false);
            }
        }
    }
}
