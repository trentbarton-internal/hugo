package uk.co.trentbarton.hugo.activities;

import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.trentbarton.hugo.customadapters.FindStopAdapter;
import uk.co.trentbarton.hugo.customviewcontrollers.MapHelper;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.R;

public class StopSearchActivity extends AppCompatActivity {

    ArrayList<Stop> stopList;
    ArrayList<Stop> selectedStops;
    ListView listView;
    EditText finder;
    FindStopAdapter listAdapter;
    ProgressBar listProgress;
    TextTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_search);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Database db = new Database(this);
        stopList = db.getAllStops();
        listView = findViewById(R.id.act_stop_search_stopList);
        finder = findViewById(R.id.act_stop_search_text);
        listAdapter = new FindStopAdapter(this, new ArrayList<Stop>());
        listView.setAdapter(listAdapter);
        listProgress = (ProgressBar) findViewById(R.id.act_stop_search_progress);
        task = new TextTask();
        assignListeners();
        finder.requestFocus();

    }

    private void assignListeners() {

        finder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                performRegExpCheck(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable text) {

            }
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            ArrayList<Stop> stops = new ArrayList<>();
            stops.add(listAdapter.getItem(i));
            MapHelper.getInstance().setSelectedStops(stops);
            loadHome();
        });

        listView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

    }

    private void hideKeyboard(){

        try{

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = listView;
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                throw new Exception("No view's have been drawn yet!");
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch(Exception ignore){
            
        }
    }

    private void loadHome() {
        this.finish();
    }

    private void performRegExpCheck(String expression){
        listView.setVisibility(View.GONE);
        listProgress.setVisibility(View.VISIBLE);
        task.cancel(true);
        task = new TextTask();
        task.execute(expression);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                MapHelper.getInstance().setSelectedStops(selectedStops);
                loadHome();
                return true;
            case R.id.menu_view_as_map:
                MapHelper.getInstance().setSelectedStops(selectedStops);
                loadHome();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        MapHelper.getInstance().setSelectedStops(selectedStops);
        super.onBackPressed();
    }

    public class TextTask extends AsyncTask<String, Void, ArrayList<Stop>> {

        @Override
        protected ArrayList<Stop> doInBackground(String... strings) {

            String text = strings[0].toLowerCase();
            Pattern roughMatch = Pattern.compile(".*\\Q" + text + "\\E.*");
            Pattern bestMatch = Pattern.compile("^\\Q" + text + "\\E.*");
            Matcher bestMatcher;
            Matcher roughMatcher;

            ArrayList<Stop> bestMatches = new ArrayList<>();
            ArrayList<Stop> roughMatches = new ArrayList<>();

            if(text.length() < 3){
                return new ArrayList<>();
            }

            for(Stop s : stopList){

                if(bestMatches.size() > 50){
                    break;
                }

                String stopName = s.getStopName().toLowerCase();
                String stopLocality;
                if(s.getLocality() == null){
                    stopLocality = "";
                }else{
                    stopLocality = s.getLocality().toLowerCase();
                }

                bestMatcher = bestMatch.matcher(stopName);
                if(bestMatcher.matches()){
                    bestMatches.add(s);
                    continue;
                }else{
                    roughMatcher = roughMatch.matcher(stopName);
                    if(roughMatcher.matches()){
                        roughMatches.add(s);
                        continue;
                    }
                }

                //Now test Locality
                bestMatcher = bestMatch.matcher(stopLocality);
                if(bestMatcher.matches()){
                    bestMatches.add(s);
                }else{
                    roughMatcher = roughMatch.matcher(stopLocality);
                    if(roughMatcher.matches()){
                        roughMatches.add(s);
                    }
                }
            }

            bestMatches.addAll(roughMatches);
            return bestMatches;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {
            selectedStops = stops;
            listAdapter = new FindStopAdapter(StopSearchActivity.this, stops);
            listView.setAdapter(listAdapter);
            listView.invalidate();
            listView.setVisibility(View.VISIBLE);
            listProgress.setVisibility(View.GONE);
        }
    }
}
