package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.R;

public class WalkingJourneyStep extends JourneyStep {


    public WalkingJourneyStep(JSONObject object) throws Exception{
        super(object);
    }

    @Override
    public View getView(Context context) {
        mContext = context;
        if(mView == null){
            mView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_walking, null);
            init();
        }

        return mView;
    }

    private void init() {

        TextView instructions = mView.findViewById(R.id.instructions_holder);
        TextView travelTime = mView.findViewById(R.id.travel_time_text);
        RelativeLayout holder = mView.findViewById(R.id.holder);

        holder.setOnClickListener(v -> {
            this.itemClicked();
        });

        instructions.setText(this.getInstructions());
        travelTime.setText(this.getFormattedDurationText());

    }
}
