package uk.co.trentbarton.hugo.customview.SlideUpMenu;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.dataholders.ServiceFilter;
import uk.co.trentbarton.hugo.interfaces.SlideUpMenuItemClickedListener;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.tools.Tools;
import uk.co.trentbarton.hugo.R;

public class SlideUpMenu extends RelativeLayout {

    ArrayList<SlideUpMenuItem> menuItems;
    LinearLayout mClickProofBackground, mMenuHolder, mMenuItemsHolder;
    TextView mCancelButton;
    SlideUpMenuItemClickedListener mListener;
    private State mState = State.WORKING;
    OnSlideUpListener mSlideUpListener;
    OnSlideDownListener mSlideDownListener;
    protected String cancelButtonText = "Close";
    private OnClickListener onCloseListener;

    public interface OnSlideUpListener{
        public void onSlideUp();
    }

    public interface OnSlideDownListener{
        public void onSlideDown();
    }

    private enum State{
        WAITING, WORKING
    }

    public SlideUpMenu(Context context) {
        super(context);
        init();
    }

    public SlideUpMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideUpMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SlideUpMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        menuItems = new ArrayList<>();

        //Set the click proof background
        mClickProofBackground = new LinearLayout(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //mClickProofBackground.setAlpha(0.5f);
        mClickProofBackground.setBackgroundColor(getResources().getColor(R.color.background_light_grey));
        mClickProofBackground.setLayoutParams(params);
        mClickProofBackground.setOnClickListener(v -> {
            //Do Nothing just consume the click event
        });

        this.addView(mClickProofBackground);

        mCancelButton = new TextView(getContext());
        mCancelButton.setText(cancelButtonText);
        mCancelButton.setId(Tools.generateViewId());
        mCancelButton.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_text_colour));
        mCancelButton.setGravity(Gravity.CENTER);
        mCancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mCancelButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.white_background_10dp_corners));
        RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(60));
        cancelParams.setMargins(Metrics.densityPixelsToPixels(20),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(20),Metrics.densityPixelsToPixels(20));
        cancelParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mCancelButton.setLayoutParams(cancelParams);

        mCancelButton.setOnClickListener(v -> {

            if(onCloseListener != null){
                onCloseListener.onClick(v);
            }

            this.slideDown();
        });

        this.addView(mCancelButton);

        mMenuHolder = new LinearLayout(getContext());
        mMenuHolder.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams menuHolderParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(120));
        menuHolderParams.addRule(RelativeLayout.ABOVE, mCancelButton.getId());
        menuHolderParams.setMargins(Metrics.densityPixelsToPixels(20),Metrics.densityPixelsToPixels(20),Metrics.densityPixelsToPixels(20),Metrics.densityPixelsToPixels(10));
        mMenuHolder.setLayoutParams(menuHolderParams);

        this.addView(mMenuHolder);

        ScrollView sv = new ScrollView(getContext());
        RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sv.setLayoutParams(scrollParams);
        mMenuHolder.addView(sv);

        mMenuItemsHolder = new LinearLayout(getContext());
        mMenuItemsHolder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.white_background_10dp_corners));
        mMenuItemsHolder.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams menuItemsParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMenuItemsHolder.setLayoutParams(menuItemsParams);

        sv.addView(mMenuItemsHolder);
        mClickProofBackground.setVisibility(GONE);
        this.setVisibility(GONE);
        mCancelButton.setTranslationY(500);
        this.mState = State.WAITING;
    }

    public void setOnCloseListener(OnClickListener l){
        onCloseListener = l;
    }


    public void setSlideUpListener(OnSlideUpListener l){
        mSlideUpListener = l;
    }

    public void setSlideDownListener(OnSlideDownListener l){
        mSlideDownListener = l;
    }

    public void assignItems(ArrayList<SlideUpMenuItem> items){
        menuItems = items;
        mMenuItemsHolder.removeAllViewsInLayout();
        createItemViews();
        this.invalidate();
    }

    private void createItemViews() {

        int i = 0;
        int height = 0;

        for(SlideUpMenuItem item : menuItems){
            item.assignPosition(i++);

            if(i == menuItems.size()){
                item.setAsLastItem();
            }

            mMenuItemsHolder.addView(item.getView());

            if(item instanceof  SlideUpTextItem){
                height += Metrics.densityPixelsToPixels(122);
            }else{
                height += Metrics.densityPixelsToPixels(61);
            }
        }

        //Height is 20 padding top, menuholderHeight + 20 padding + 60 for cancel Button + 20 padding again
        if(height > Metrics.densityPixelsToPixels(400)){
            height = Metrics.densityPixelsToPixels(400);
        }

        ViewGroup.LayoutParams params = mMenuHolder.getLayoutParams();
        params.height = height;
        mMenuHolder.setLayoutParams(params);

        mCancelButton.setTranslationY(height + Metrics.densityPixelsToPixels(120));
        mMenuHolder.setTranslationY(height + Metrics.densityPixelsToPixels(120));
    }

    public void selectAllServiceItems(){

        for(SlideUpMenuItem item : this.menuItems){

            if(item instanceof SlideUpServiceItem){
                ((SlideUpServiceItem) item).forceToSelected();
            }
        }
    }

    public void overrideCancelText(String newText){

        cancelButtonText = newText;

        if(mCancelButton != null){
            mCancelButton.setText(newText);
        }

    }

    public void slideUp(){

        if(mState == State.WORKING){
            return;
        }

        if(mSlideUpListener != null){
            mSlideUpListener.onSlideUp();
        }

        this.setVisibility(VISIBLE);
        mClickProofBackground.setVisibility(VISIBLE);
        mMenuHolder.setVisibility(VISIBLE);
        mClickProofBackground.animate().alpha(0.7f).setDuration(200).setStartDelay(50).start();
        mCancelButton.animate().translationY(0).setDuration(250).setInterpolator(new DecelerateInterpolator(1.2f)).start();
        mMenuHolder.animate().translationY(0).setDuration(250).setInterpolator(new DecelerateInterpolator(1.2f)).withEndAction(() -> mState = State.WAITING).start();
    }

    public void slideDown(){

        if(mState == State.WORKING){
            return;
        }

        mState = State.WORKING;

        mMenuHolder.animate().translationY(mMenuHolder.getMeasuredHeight() + Metrics.densityPixelsToPixels(20)).setDuration(250).setInterpolator(new AccelerateInterpolator(1.2f)).start();
        mCancelButton.animate().translationY(mMenuHolder.getMeasuredHeight() + Metrics.densityPixelsToPixels(20)).setDuration(250).setInterpolator(new AccelerateInterpolator(1.2f)).start();
        mClickProofBackground.animate().alpha(0).setDuration(200).setStartDelay(50).withEndAction(() -> ((Activity)getContext()).runOnUiThread(() -> {
                    mState = State.WAITING;
                    mMenuHolder.setVisibility(GONE);
                    mClickProofBackground.setVisibility(GONE);
                    this.setVisibility(GONE);

                    if(mSlideDownListener != null){
                        mSlideDownListener.onSlideDown();
                    }


                }
        )).start();
    }

    public void assignItemClickedListener(SlideUpMenuItemClickedListener l){
        mListener = l;
    }

    public ServiceFilter generateServiceFilter(){

        ServiceFilter filter = new ServiceFilter();

        for(SlideUpMenuItem item : this.menuItems){

            if(item instanceof SlideUpServiceItem){

                if(!((SlideUpServiceItem)item).isSelected()){
                    filter.addServiceName(((SlideUpServiceItem)item).getServiceName());
                }
            }
        }

        return filter;

    }




}
