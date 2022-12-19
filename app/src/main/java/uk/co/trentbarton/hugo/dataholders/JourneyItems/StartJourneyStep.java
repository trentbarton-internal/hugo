package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.R;

public class StartJourneyStep extends JourneyStep {

    private String startAddress;

    public StartJourneyStep(JSONObject object) throws Exception{
        super(object);

        startAddress = object.getString("start_address");
    }

    @Override
    public View getView(Context context) {
        mContext = context;
        if(mView == null){
            mView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_start, null);
            init();
        }

        return mView;
    }

    private void init() {

        TextView startAddressText = mView.findViewById(R.id.startAddressTextView);
        startAddressText.setText(this.getInstructions());

    }

    @Override
    public String getInstructions(){
        return startAddress;
    }

}
