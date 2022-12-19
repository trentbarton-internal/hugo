package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.R;

public class FinishJourneyStep extends JourneyStep {

    private String endAddress;
    private String totalDistance;

    public FinishJourneyStep(JSONObject object) throws Exception {
        super(object);
        endAddress = object.getString("end_address");
        this.setStartPosition(this.getEndPosition());
    }

    public void setTotalDistance(String distance){
        totalDistance = "TOTAL DISTANCE " + distance;
    }

    public String getTotalDistance(){
        return this.totalDistance;
    }

    @Override
    public View getView(Context context) {

        mContext = context;

        if(mView == null){
            mView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_finish, null);
            init();
        }

        return mView;
    }

    private void init() {

        TextView instructions = mView.findViewById(R.id.instructions_holder);
        TextView totalDistance = mView.findViewById(R.id.totalDistance);
        RelativeLayout holder = mView.findViewById(R.id.holder);

        holder.setOnClickListener(v -> {
            this.itemClicked();
        });

        instructions.setText(this.getInstructions());
        totalDistance.setText(this.getTotalDistance());
    }

    @Override
    public String getInstructions(){
        return "Arrive at " + endAddress;
    }
}
