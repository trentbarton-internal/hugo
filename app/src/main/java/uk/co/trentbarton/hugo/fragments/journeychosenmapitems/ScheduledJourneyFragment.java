package uk.co.trentbarton.hugo.fragments.journeychosenmapitems;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.ScheduledJourneyStep;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;

public class ScheduledJourneyFragment extends Fragment {

    private int mJourneyIndex, mStepIndex;
    private ScheduledJourneyStep mJourneyStep;
    private TextView serviceNameText, destinationText, scheduledTextTime;
    private ImageView serviceColour;
    private LinearLayout mangoHolder;

    public static ScheduledJourneyFragment newInstance(int journeyIndex, int stepIndex){
        ScheduledJourneyFragment fragment = new ScheduledJourneyFragment();
        Bundle args = new Bundle();
        args.putInt("journeyIndex", journeyIndex);
        args.putInt("stepIndex", stepIndex);
        fragment.setArguments(args);
        return fragment;
    }


    protected void readBundle(Bundle bundle) {
        if (bundle != null) {
            mJourneyIndex = bundle.getInt("journeyIndex",0);
            mStepIndex = bundle.getInt("stepIndex", 0);
        }
        try{
            mJourneyStep = (ScheduledJourneyStep) GlobalData.getInstance().getJourneyData().get(mJourneyIndex).getSteps().get(mStepIndex);
        }catch(Exception ignore){}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            readBundle(getArguments());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        readBundle(savedInstanceState);
        View view = inflater.inflate(R.layout.journey_map_item_scheduled_fragment, container, false);
        serviceNameText = view.findViewById(R.id.journey_map_item_service_name);
        serviceColour = view.findViewById(R.id.journey_map_item_service_colour);
        destinationText = view.findViewById(R.id.journey_map_item_destination);
        mangoHolder = view.findViewById(R.id.journey_map_item_mango_holder);
        scheduledTextTime = view.findViewById(R.id.journey_map_item_scheduled_time);

        if(mJourneyStep != null) init();
        return view;
    }

    private void init() {

        serviceNameText.setText(mJourneyStep.getServiceName());
        serviceColour.setBackgroundColor(mJourneyStep.getServiceColour());
        destinationText.setText("towards " + mJourneyStep.getServiceDestination());

        if(mJourneyStep.isBusHasMango()){
            mangoHolder.setVisibility(View.VISIBLE);
        }else{
            mangoHolder.setVisibility(View.GONE);
        }

        scheduledTextTime.setText(mJourneyStep.getDepartureTimeText());


    }
}
