package uk.co.trentbarton.hugo.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.joda.time.LocalDateTime;

import java.util.Objects;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.JourneyHttpDataParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomTimeDialog;
import uk.co.trentbarton.hugo.fragments.JourneyChosenFragment;
import uk.co.trentbarton.hugo.fragments.JourneyResultsFragment;

public class JourneyResults extends AppCompatActivity {

    JourneyResultsFragment mJourneyResultsFragment;
    boolean showingMapScreen = false;
    private MenuItem changeTimeMenuItem;
    private boolean isLeavingTime = true;
    private boolean isTimeNow = true;
    private LocalDateTime timeRequested;
    JourneyChosenFragment mJourneyChosenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_results);

        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle("Journey results");
        }

        if(getIntent().getExtras() != null){
            int index = getIntent().getExtras().getInt("index", -1);
            if(index != -1){
                //Push the user through to the results
                GlobalData.getInstance().setJourneyData(HugoPreferences.getLastJourneyData(this));
                Journey journey = null;
                try{
                    journey = GlobalData.getInstance().getJourneyData().get(0);
                }catch(Exception e){
                    Toast.makeText(this, "Oops! something didn't load right, let's try that again", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainNavigationActivity.class);
                    startActivity(intent);
                    this.finish();
                }

                assert journey != null;
                TomTomPlace from = new TomTomPlace(journey.getStartPosition().latitude, journey.getStartPosition().longitude, journey.getFriendlyFromName(), "");
                TomTomPlace to = new TomTomPlace(journey.getEndPosition().latitude, journey.getEndPosition().longitude, journey.getFriendlyToName(), "");
                JourneyParams.getInstance().setFromPlace(from);
                JourneyParams.getInstance().setToPlace(to);
                Fragment fragment = new JourneyChosenFragment();
                Bundle args = new Bundle();
                args.putInt("index",index);
                fragment.setArguments(args);
                try{
                    getSupportActionBar().setTitle("Journey details");
                    if(changeTimeMenuItem != null) changeTimeMenuItem.setVisible(false);
                }catch(Exception ignore){}
                try{
                    this.loadResultsMapFragment(fragment);
                }catch(Exception e){
                    Toast.makeText(this, "Oops! something didn't load right, let's try that again", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainNavigationActivity.class);
                    startActivity(intent);
                    this.finish();
                }
                return;
            }
        }

        try{
            loadResultsFragment();
        }catch(Exception e){
            Toast.makeText(this, "Oops! something didn't load right, let's try that again", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainNavigationActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.journey_results_menu, menu);
        changeTimeMenuItem = menu.findItem(R.id.menu_change_time);
        try{
            if(Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().equalsIgnoreCase("Journey details")){
                changeTimeMenuItem.setVisible(false);
            }
        }catch(Exception ignore){}

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int changeTimeId = R.id.menu_change_time;

        switch (item.getItemId()) {
            case android.R.id.home:
                if(showingMapScreen){
                    mJourneyChosenFragment.removeAllViews();
                    loadResultsFragment();
                }else{
                    onBackPressed();
                }
                return true;
            case changeTimeId:
                showChangeTimeDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public JourneyHttpDataParams getJourneyParams(){
        JourneyHttpDataParams params = new JourneyHttpDataParams(this)
                .addToPlace(JourneyParams.getInstance().getToPlace())
                .addFromPlace(JourneyParams.getInstance().getFromPlace());

        if(!isTimeNow){
            if(isLeavingTime){
                params.addDepartureTime(timeRequested);
            }else{
                params.addArrivalTime(timeRequested);
            }
        }

        return params;

    }

    private void showChangeTimeDialog() {

        CustomTimeDialog dialog = new CustomTimeDialog(this);
        dialog.setAcceptButtonListener(() -> {
            isLeavingTime = dialog.isLeavingTime();
            isTimeNow = dialog.isTimeNow();
            timeRequested = dialog.getDateSet();

            if(mJourneyResultsFragment != null){
                mJourneyResultsFragment.runDataTask();
            }

            return true;
        });
        dialog.show();
        dialog.setIsLeavingTime(isLeavingTime);
        if(isTimeNow){
            dialog.setIsNow(true);
        }else{
            dialog.setDate(timeRequested.toDate());
        }
    }

    private void loadResultsFragment() {
        showingMapScreen = false;

        try{
            Objects.requireNonNull(getSupportActionBar()).setTitle("Journey results");
            if(changeTimeMenuItem != null) changeTimeMenuItem.setVisible(true);
        }catch(Exception ignore){}

        if(mJourneyResultsFragment == null){
            mJourneyResultsFragment = new JourneyResultsFragment();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.mainWindow, mJourneyResultsFragment);
        transaction.commit();
    }

    public void loadResultsMapFragment(Fragment newFragment){

        mJourneyChosenFragment = (JourneyChosenFragment) newFragment;
        showingMapScreen = true;
        try{
            Objects.requireNonNull(getSupportActionBar()).setTitle("Journey details");
            if(changeTimeMenuItem != null) changeTimeMenuItem.setVisible(false);
        }catch(Exception ignore){}

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.mainWindow, newFragment);
        transaction.commit();

    }

    @Override
    public void onBackPressed(){
        if(showingMapScreen){
            loadResultsFragment();
        }else{
            super.onBackPressed();
        }
    }
}
