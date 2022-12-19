package uk.co.trentbarton.hugo.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.activities.JourneyResults;
import uk.co.trentbarton.hugo.activities.MainNavigationActivity;
import uk.co.trentbarton.hugo.customadapters.JourneyResultsListAdapter;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.JourneyHttpDataParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.R;

public class JourneyResultsFragment extends Fragment {

    ListView listview;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView fromPlace, toPlace;
    ImageView switchPlacesButton;
    JourneyResultsListAdapter mAdapter;
    private boolean refreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_journey_results, container, false);

        fromPlace = view.findViewById(R.id.fromPlaceText);
        toPlace = view.findViewById(R.id.toPlaceText);
        switchPlacesButton = view.findViewById(R.id.switch_icon);

        try{
            if(JourneyParams.getInstance().getFromPlace() == null || JourneyParams.getInstance().getFromPlace().getName().equalsIgnoreCase("")){
                fromPlace.setText(GlobalData.getInstance().getJourneyData().get(0).getStartAddress());
            }else{
                fromPlace.setText(JourneyParams.getInstance().getFromPlace().getName());
            }

            if(JourneyParams.getInstance().getToPlace() == null || JourneyParams.getInstance().getToPlace().getName().equalsIgnoreCase("")){
                toPlace.setText(GlobalData.getInstance().getJourneyData().get(0).getEndAddress());
            }else{
                toPlace.setText(JourneyParams.getInstance().getToPlace().getName());
            }

        }catch(Exception e){
            //This may happen if the app closes and RoutePlanningInfo is lost so return to journey planner screen
            Intent intent = new Intent(getActivity(), MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }

        switchPlacesButton.setOnClickListener(view12 -> {

            String fromName = fromPlace.getText().toString();
            String toName = toPlace.getText().toString();
            fromPlace.setText(toName);
            toPlace.setText(fromName);
            TomTomPlace to = JourneyParams.getInstance().getToPlace();
            TomTomPlace from = JourneyParams.getInstance().getFromPlace();
            JourneyParams.getInstance().setFromPlace(to);
            JourneyParams.getInstance().setToPlace(from);
            runDataTask();

        });

        listview = view.findViewById(R.id.resultsList);
        mAdapter = new JourneyResultsListAdapter(getContext(), this);

        try {
            listview.setAdapter(mAdapter);
        }catch(Exception e){
            //This has happened and caused a null pointer exception for some reason
            Toast.makeText(getActivity(),"An error occurred please try again",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshing = true;
            runDataTask();
        });

        listview.setOnItemClickListener((adapterView, view1, i, l) -> {

            if(refreshing){
                Toast.makeText(getActivity(),"Please wait for the refresh to finish running",Toast.LENGTH_SHORT).show();
            }else{
                JourneyResults activity = (JourneyResults) getActivity();
                Fragment fragment = new JourneyChosenFragment();
                Bundle args = new Bundle();
                args.putInt("index",i);
                fragment.setArguments(args);
                HugoPreferences.setLastJourneyItemChosen(getContext(), i);
                try{
                    activity.loadResultsMapFragment(fragment);
                }catch(Exception e){
                    //ignore for now
                }
            }
        });

        return view;
    }

    public void runDataTask(){
        swipeRefreshLayout.setRefreshing(true);
        JourneyHttpDataParams params = ((JourneyResults)getActivity()).getJourneyParams();
        DataRequestTask task = new DataRequestTask(params);
        task.setOnTaskCompletedListener(bool -> {
            refreshing = false;
            swipeRefreshLayout.setRefreshing(false);
            if (bool) {
                //Task worked so get array of results
                if(task.getResponse() == null){
                    try{
                        Toast.makeText(getContext(), "Search returned 0 results", Toast.LENGTH_SHORT).show();
                    }catch(Exception ignore){}
                }else{
                    if(task.getResponse() instanceof  ArrayList){
                        ArrayList<Journey> list = (ArrayList<Journey>) task.getResponse();
                        //Launch the new Activity with this data
                        GlobalData.getInstance().setJourneyData(list);
                        mAdapter.refreshData();
                    }
                    else{
                        try{
                            Toast.makeText(getContext(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                        }catch(Exception ignore){}

                    }
                }
            } else {
                try{
                    Toast.makeText(getContext(), task.getErrorMessage(), Toast.LENGTH_LONG).show();
                }catch(Exception ignore){}
            }
        });
        task.execute(getContext());
    }


}
