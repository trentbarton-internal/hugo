package uk.co.trentbarton.hugo.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import uk.co.trentbarton.hugo.customadapters.ServiceChooserAdapter;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetServicesParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.SetSubscribedServiceParams;
import uk.co.trentbarton.hugo.dataholders.Service;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.R;

public class ChooseServiceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    ListView serviceListView;
    ServiceChooserAdapter mAdapter;
    RelativeLayout refreshingLayout;
    TextView errorMessage;
    private List<Service> fullServices;
    private List<Service> filteredServices;
    private MenuItem selectAllMenuItem, unselectAllMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_service);

        if(getSupportActionBar() != null){
            setupActionBar(getSupportActionBar());
        }

        init();
        assignListeners();
        startTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.service_chooser_menu, menu);
        selectAllMenuItem = menu.findItem(R.id.selectAll);
        unselectAllMenuItem = menu.findItem(R.id.unselectAll);
        MenuItem searchItem = menu.findItem(R.id.searchBar);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Services");
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(true);

        searchView.setOnSearchClickListener(v -> {
            unselectAllMenuItem.setVisible(false);
            selectAllMenuItem.setVisible(false);
            searchView.requestFocus();
        });

        searchView.setOnCloseListener(() -> {
            onQueryTextChange("");
            resetShowAllButtons();
            return false;
        });

        searchView.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                onQueryTextChange("");
                resetShowAllButtons();
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.requestFocusFromTouch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onQueryTextChange("");
                return false;
            }
        });

        resetShowAllButtons();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        final int selectAll = R.id.selectAll;
        final int unSelectAll = R.id.unselectAll;

        switch (item.getItemId()) {
            case selectAll:
                selectAll();
                return false;
            case unSelectAll:
                unselectAll();
                return false;
            case android.R.id.home:
                saveChanges();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionBar(ActionBar actionBar) {

        actionBar.setTitle("Subscribe to service");

    }

    private void init() {

        filteredServices = new ArrayList<>();
        fullServices = new ArrayList<>();
        mAdapter = new ServiceChooserAdapter(this, filteredServices);
        serviceListView = findViewById(R.id.service_list_view);
        refreshingLayout = findViewById(R.id.refreshingLayout);
        errorMessage = findViewById(R.id.errorMessage);
        serviceListView.setAdapter(mAdapter);
        mAdapter.setOnServiceClickedListener((position, item) -> {
            item.setSubscribed(!item.isSubscribed());
            mAdapter.notifyDataSetChanged();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll(){
        for(Service service : fullServices){
            service.setSubscribed(true);
            FirebaseMessaging.getInstance().subscribeToTopic(service.getTopicName());
        }

        mAdapter.notifyDataSetChanged();
        resetShowAllButtons();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void unselectAll(){

        for(Service service : fullServices){
            service.setSubscribed(false);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(service.getTopicName());
        }

        mAdapter.notifyDataSetChanged();
        resetShowAllButtons();
    }

    private boolean areAnyServicesSelected(){

        if(this.fullServices == null || this.fullServices.isEmpty()){
            return false;
        }

        for(Service service : fullServices){
            if(service.isSubscribed()){return true;}
        }

        return false;

    }

    private void saveChanges(){

        List<String> servicesSubscribed = new ArrayList<>();

        for(Service service : fullServices){
            if(service.isSubscribed()){
                servicesSubscribed.add(service.getServiceName());
            }
        }

        HugoPreferences.setNumberOfServicesSubscribed(this, servicesSubscribed.size());
        String[] servicesArray = new String[servicesSubscribed.size()];

        for(int i = 0; i < servicesSubscribed.size(); i++){
            servicesArray[i] = servicesSubscribed.get(i);
        }

        SetSubscribedServiceParams mParams = new SetSubscribedServiceParams(this);
        mParams.addServicesArray(servicesArray);
        DataRequestTask task = new DataRequestTask(mParams);
        task.execute(this);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void startTask() {

        GetServicesParams mParams = new GetServicesParams(this);
        DataRequestTask task = new DataRequestTask(mParams);
        task.setOnTaskCompletedListener(bool -> {

            if(bool){
                refreshingLayout.setVisibility(View.GONE);
                ArrayList<Service> services = (ArrayList<Service>)task.getResponse();
                fullServices.addAll(services);
                filteredServices.clear();
                filteredServices.addAll(services);
                mAdapter.notifyDataSetChanged();
                resetShowAllButtons();
            }else{
                showErrorMessage();
            }
        });

        task.execute(this);

    }

    private void resetShowAllButtons(){

        if(areAnyServicesSelected()){
            unselectAllMenuItem.setVisible(true);
            selectAllMenuItem.setVisible(false);
        }else{
            selectAllMenuItem.setVisible(true);
            unselectAllMenuItem.setVisible(false);
        }

    }

    private void showErrorMessage() {
        refreshingLayout.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void assignListeners(){
        mAdapter.setOnServiceClickedListener((position, item) -> {
            filteredServices.get(position).setSubscribed(!filteredServices.get(position).isSubscribed());
            Service service = filteredServices.get(position);

            if(service.isSubscribed()){
                FirebaseMessaging.getInstance().subscribeToTopic(service.getTopicName());
            }else{
                FirebaseMessaging.getInstance().unsubscribeFromTopic(service.getTopicName());
            }
            mAdapter.notifyDataSetChanged();
            resetShowAllButtons();
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onQueryTextChange(String newText) {

        if(newText.equalsIgnoreCase("")){
            //Add them all back in
            filteredServices.clear();
            filteredServices.addAll(fullServices);
            mAdapter.notifyDataSetChanged();
            return false;
        }


        List<Service> tempServices = new ArrayList<>();

        for(Service service : fullServices){

            if(service.getServiceName().toLowerCase().contains(newText.toLowerCase())){
                tempServices.add(service);
            }
        }

        filteredServices.clear();
        filteredServices.addAll(tempServices);
        mAdapter.notifyDataSetChanged();
        return false;

    }
}
