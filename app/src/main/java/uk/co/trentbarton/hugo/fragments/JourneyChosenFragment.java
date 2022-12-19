package uk.co.trentbarton.hugo.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import uk.co.trentbarton.hugo.activities.MainNavigationActivity;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.JourneyStep;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.interfaces.OnStepClickedListener;
import uk.co.trentbarton.hugo.R;

public class JourneyChosenFragment extends Fragment implements OnStepClickedListener {

    private int chosenIndex;
    private Journey mJourney;
    private LinearLayout mContentHolder, mJourneyDetailsMapHolder;
    private JourneyChosenMapFragment mMapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_details, container, false);
        mJourneyDetailsMapHolder = view.findViewById(R.id.journey_details_map_holder);
        mJourneyDetailsMapHolder.setTranslationX(Resources.getSystem().getDisplayMetrics().widthPixels);

        try{
            chosenIndex = getArguments().getInt("index");
            mJourney = GlobalData.getInstance().getJourneyData().get(chosenIndex);
            assignViews(view);
            loadContent();
        }catch(Exception ex){
            mJourney = null;
            Intent intent = new Intent(getContext(), MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fromActivity", "JourneyResultsActivity");
            startActivity(intent);
            try{
                getActivity().finish();
            }catch(Exception ignore){}
        }

        mMapFragment = JourneyChosenMapFragment.newInstance(chosenIndex);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.journey_details_map_holder, mMapFragment).commit();

        return view;

    }

    private void assignViews(View view) {
        mContentHolder = view.findViewById(R.id.resultsContentHolder);
    }

    private void loadContent(){

        for(JourneyStep step : mJourney.getSteps()){
            mContentHolder.addView(step.getView(getActivity()));
            step.setOnStepClickedListener(this);
        }
    }

    public void removeAllViews(){
        mContentHolder.removeAllViews();
    }

    public void animateMap(){
        mJourneyDetailsMapHolder.animate().translationX(0).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
    }

    public void closeMap(){
        mJourneyDetailsMapHolder.animate().translationX(Resources.getSystem().getDisplayMetrics().widthPixels).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
    }


    @Override
    public void onStepClicked(JourneyStep step) {

        int index = mJourney.getSteps().indexOf(step);
        mMapFragment.loadChild(index);
        animateMap();

    }

}
