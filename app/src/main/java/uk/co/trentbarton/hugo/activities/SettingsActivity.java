package uk.co.trentbarton.hugo.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import java.io.Serializable;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.fragments.ContactUsFragment;
import uk.co.trentbarton.hugo.fragments.HelpImproveFragment;
import uk.co.trentbarton.hugo.fragments.NotificationsFragment;

public class SettingsActivity extends AppCompatActivity {

    Fragment fragment;
    public static final String OPTION_KEY = "option";

    public enum SettingsScreen implements Serializable {
        MESSAGE_SETTINGS("Messages from hugo"),
        IMPROVE_HUGO("Help improve hugo"),
        CONTACT_US("Contact us");
        public String titleName;

        SettingsScreen(String s){
            titleName = s;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();

    }

    private void init() {

        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            SettingsScreen option = (SettingsScreen) getIntent().getSerializableExtra(OPTION_KEY);

            if(option == null){
                loadHome();
            }else{
                actionbar.setTitle(option.titleName);
                setScreen(option);
            }
        }

    }

    private void setScreen(SettingsScreen option) {

        fragment = null;

        switch(option){
            case CONTACT_US:
                fragment = new ContactUsFragment();
                break;
            case IMPROVE_HUGO:
                fragment = new HelpImproveFragment();
                break;
            case MESSAGE_SETTINGS:
                fragment = new NotificationsFragment();
                break;
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.settings_main_window, fragment);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        loadHome();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                loadHome();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHome() {
        Intent intent = new Intent(SettingsActivity.this, MainNavigationActivity.class);
        GlobalData.getInstance().setNavigateToFragmentPosition(3);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}