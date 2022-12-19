package uk.co.trentbarton.hugo.customview;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class JourneyOverviewItem extends RelativeLayout {

    public JourneyOverviewItem(Context context) {
        super(context);
    }

    public JourneyOverviewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JourneyOverviewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public JourneyOverviewItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }




}
