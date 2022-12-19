package uk.co.trentbarton.hugo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.fragments.JourneyFragment;
import uk.co.trentbarton.hugo.fragments.LiveFragment;
import uk.co.trentbarton.hugo.fragments.MapFragment;
import uk.co.trentbarton.hugo.fragments.MessagesFragment;
import uk.co.trentbarton.hugo.fragments.SettingsFragment;
import uk.co.trentbarton.hugo.interfaces.RefreshInterface;
import uk.co.trentbarton.hugo.notifications.FirebaseMessageReceived;
import uk.co.trentbarton.hugo.widget.RealtimeWidgetProvider;

public class MainNavigationActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback{


    ViewPager2 mViewPager2;
    ImageView mJourneyButton, mLiveButton, mSettingsButton, mAlertsButton;
    TextView mAlertsPeekNotification, mJourneyText, mLiveText, mSettingsText, mAlertsText;
    private static final double NAV_SELECTED_IMAGE_RESIZE = 0.7;
    private int CURRENT_POSITION = -1;
    FragmentStateAdapter fragmentStateAdapter;
    BroadcastReceiver mReceiver;
    private static final String TAG = MainNavigationActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBroadcastReceiver();
        setContentView(R.layout.activity_main_navigation);
        initUI();

        if(getIntent().hasExtra("function")){
            if(getIntent().getStringExtra("function").equals(RealtimeWidgetProvider.FUNCTION_PREDICTION_CLICKED)){
                loadDataFromWidget();
            }
        }

        Thread thread = new Thread(() -> {
            try{
                Thread.sleep(100);
                runOnUiThread(() -> {
                    fragmentStateAdapter.createFragment(0).onPause();
                    ((LiveFragment)fragmentStateAdapter.createFragment(0)).startMapAsync();
                    fragmentStateAdapter.createFragment(0).onResume();

                });
            }catch(Exception e){
                Log.e(TAG, e.getMessage(), e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void createBroadcastReceiver() {

        if(mReceiver == null){
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        //((RefreshInterface) adapterViewPager.getItem(3)).RefreshUI();
                        ((RefreshInterface) fragmentStateAdapter.createFragment(3)).RefreshUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "New intent received");
        if(intent.hasExtra("function")){
            if(intent.getStringExtra("function").equals(RealtimeWidgetProvider.FUNCTION_PREDICTION_CLICKED)){
                loadDataFromWidget();
            }
        }
    }

    private void loadDataFromWidget() {

        String atcoCode = getIntent().getStringExtra("atcoCode");
        int vehicleNumber = getIntent().getIntExtra("vehicleNumber", 0);
        Log.d(TAG, String.format("Got data from intent, atcoCode: %s, vehicleNumber: %d", atcoCode, vehicleNumber));
        moveTabs(0);
        //((LiveFragment)adapterViewPager.getItem(0)).setWidgetData(atcoCode, vehicleNumber, MainNavigationActivity.this);
        ((LiveFragment)fragmentStateAdapter.createFragment(0)).setWidgetData(atcoCode, vehicleNumber, MainNavigationActivity.this);

    }

    public void updateUnreadMessagePeek(int numberOfUnread){

        if(numberOfUnread <= 0){
            mAlertsPeekNotification.setVisibility(View.GONE);
        }else{
            mAlertsPeekNotification.setText(String.valueOf(numberOfUnread));
            mAlertsPeekNotification.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // A permission was granted so update the UI
            //((RefreshInterface) adapterViewPager.getItem(requestCode)).RefreshUI();
            ((RefreshInterface) fragmentStateAdapter.createFragment(requestCode)).RefreshUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(FirebaseMessageReceived.REQUEST_CANCEL_ALARM)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadMessagePeek(0);
        moveTabs(GlobalData.getInstance().getNavigateToFragmentPosition());
        mViewPager2.setCurrentItem(GlobalData.getInstance().getNavigateToFragmentPosition());

        try {
            //((RefreshInterface) adapterViewPager.getItem(GlobalData.getInstance().getNavigateToFragmentPosition())).RefreshUI();
            ((RefreshInterface) fragmentStateAdapter.createFragment(GlobalData.getInstance().getNavigateToFragmentPosition())).RefreshUI();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void moveTabs(int newPosition){

        if(newPosition == CURRENT_POSITION){
            return;
        }

        GlobalData.getInstance().setNavigateToFragmentPosition(newPosition);
        ImageView currentViewSelected;
        ImageView newViewSelected;
        fragmentStateAdapter.createFragment(CURRENT_POSITION).onPause();
        ((RefreshInterface) fragmentStateAdapter.createFragment(newPosition)).RefreshUI();


        switch(CURRENT_POSITION){
            case 1:
                currentViewSelected = mJourneyButton;
                mJourneyButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_journey_grey,getTheme()));
                mJourneyText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_light_grey));
                break;
            case 2:
                currentViewSelected = mAlertsButton;
                mAlertsButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_alerts_grey,getTheme()));
                mAlertsText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_light_grey));
                break;
            case 3:
                currentViewSelected = mSettingsButton;
                mSettingsButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_settings_grey,getTheme()));
                mSettingsText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_light_grey));
                break;
            default:
                currentViewSelected = mLiveButton;
                mLiveButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_live_grey,getTheme()));
                mLiveText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_light_grey));
        }

        switch(newPosition){
            case 0:
                //((MapFragment)adapterViewPager.getItem(0)).startMapAsync();
                ((MapFragment)fragmentStateAdapter.createFragment(0)).startMapAsync();
                newViewSelected = mLiveButton;
                mLiveButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_live_dark_grey,getTheme()));
                mLiveText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_dark_grey));
                mViewPager2.setUserInputEnabled(false);
                break;
            case 1:
                //((MapFragment)adapterViewPager.getItem(1)).startMapAsync();
                ((MapFragment)fragmentStateAdapter.createFragment(1)).startMapAsync();
                newViewSelected = mJourneyButton;
                mJourneyButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_journey_dark_grey,getTheme()));
                mJourneyText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_dark_grey));
                mViewPager2.setUserInputEnabled(false);
                break;
            case 2:
                newViewSelected = mAlertsButton;
                mAlertsButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_alerts_dark_grey,getTheme()));
                mAlertsText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_dark_grey));
                mViewPager2.setUserInputEnabled(false);
                break;
            case 3:
                newViewSelected = mSettingsButton;
                mSettingsButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_settings_dark_grey,getTheme()));
                mSettingsText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_dark_grey));
                mViewPager2.setUserInputEnabled(false);
                break;
            default:
                newViewSelected = mLiveButton;
                mLiveButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.nav_live_dark_grey,getTheme()));
                mLiveText.setTextColor(ContextCompat.getColor(this,R.color.nav_text_dark_grey));
                mViewPager2.setUserInputEnabled(false);
        }

        int regularPixelPadding = newViewSelected.getPaddingBottom();
        int bigPixelPadding = (int) (regularPixelPadding * NAV_SELECTED_IMAGE_RESIZE);
        currentViewSelected.setPadding(regularPixelPadding, regularPixelPadding, regularPixelPadding, regularPixelPadding);
        newViewSelected.setPadding(bigPixelPadding, bigPixelPadding, bigPixelPadding, bigPixelPadding);
        CURRENT_POSITION = newPosition;
        //adapterViewPager.getItem(newPosition).onResume();
        fragmentStateAdapter.createFragment(newPosition).onResume();

    }

    private void initUI() {
        //mViewPager = findViewById(R.id.main_nav_viewpager);
        mViewPager2 = findViewById(R.id.main_nav_viewpager);
        mJourneyButton = findViewById(R.id.main_nav_journey_button);
        mLiveButton = findViewById(R.id.main_nav_live_button);
        mSettingsButton = findViewById(R.id.main_nav_settings_button);
        mAlertsButton = findViewById(R.id.main_nav_alerts_button);
        mAlertsPeekNotification = findViewById(R.id.main_nav_alerts_peek_notification);
        fragmentStateAdapter = new MyFragmentStateAdapter(getSupportFragmentManager(), this.getLifecycle());
        mViewPager2.setUserInputEnabled(false);
        mViewPager2.setOffscreenPageLimit(5);
        mViewPager2.setAdapter(fragmentStateAdapter);
        mJourneyText = findViewById(R.id.main_nav_journey_text);
        mLiveText = findViewById(R.id.main_nav_live_text);
        mSettingsText = findViewById(R.id.main_nav_settings_text);
        mAlertsText = findViewById(R.id.main_nav_alerts_text);


        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                moveTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLiveButton.setOnClickListener(view -> {
            GlobalData.getInstance().setNavigateToFragmentPosition(0);
            mViewPager2.setCurrentItem(0);
        });

        mJourneyButton.setOnClickListener(view -> {
            GlobalData.getInstance().setNavigateToFragmentPosition(1);
            mViewPager2.setCurrentItem(1);
        });

        mAlertsButton.setOnClickListener(view -> {
            GlobalData.getInstance().setNavigateToFragmentPosition(2);
            mViewPager2.setCurrentItem(2);
        });

        mSettingsButton.setOnClickListener(view -> {
            GlobalData.getInstance().setNavigateToFragmentPosition(3);
            mViewPager2.setCurrentItem(3);
        });

    }

    public static class MyFragmentStateAdapter extends FragmentStateAdapter {
        private final static int NUM_ITEMS = 4;
        private final SparseArray<Fragment> fragments = new SparseArray<>();

        public MyFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment f;
            if(this.fragments.get(position,null) != null){
                return fragments.get(position);
            }

            switch (position) {
                case 0:
                    f = LiveFragment.newInstance();
                    fragments.put(position, f);
                    return f;
                case 1:
                    f = JourneyFragment.newInstance();
                    fragments.put(position, f);
                    return f;
                case 2:
                    f = new MessagesFragment();
                    fragments.put(position, f);
                    return f;
                case 3:
                    f = new SettingsFragment();
                    fragments.put(position, f);
                    return f;
                default:
                    f = new LiveFragment();
                    fragments.put(position, f);
                    return f;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_ITEMS;
        }
    }
}