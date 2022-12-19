package uk.co.trentbarton.hugo.fragments.journeychosenmapitems;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.HopOffStep;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;

public class HopOffJourneyFragment extends Fragment {

    private TextView instructionsHolder;
    protected int mJourneyIndex, mStepIndex;
    protected HopOffStep mJourneyStep;

    public static HopOffJourneyFragment newInstance(int journeyIndex, int stepIndex){
        HopOffJourneyFragment fragment = new HopOffJourneyFragment();
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
            mJourneyStep = (HopOffStep) GlobalData.getInstance().getJourneyData().get(mJourneyIndex).getSteps().get(mStepIndex);
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
        View view = inflater.inflate(R.layout.journey_map_item_hop_off_fragment, container, false);
        instructionsHolder = view.findViewById(R.id.fragment_map_item_instructions);

        if(mJourneyStep != null) instructionsHolder.setText(mJourneyStep.getInstructions());

        return view;
    }
}
