package uk.co.trentbarton.hugo.activities;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.MapsInitializer;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.File;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.notifications.NotificationChannels;
import uk.co.trentbarton.hugo.tasks.UpdateTask;
import uk.co.trentbarton.hugo.tools.Constants;


public class SplashScreenActivity extends AppCompatActivity implements OnTaskCompletedListener {

    private final String TAG = SplashScreenActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fixBugs();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "screen");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Splash Screen");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        setContentView(R.layout.splash_screen);
        MapsInitializer.initialize(this);

        Database db = new Database(this);
        db.deletePlaceReferences();

        if(HugoPreferences.isFirstOpening(this)){
            //Subscribe to all notifications
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_OFFERS);
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_APP);
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_BUS);
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_TIMETABLE);
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_TIMETABLE);
        }else{
            checkAllSubscriptions();
        }

        if(HugoPreferences.isFirstOpening(this) && db.getFavouriteStops().isEmpty()){
            HugoPreferences.setAppOpened(this);
            NotificationChannels.setupNotificationChannels(this);
            startOnBoarding();
        }else{
            //Start the update task
            UpdateTask task = new UpdateTask(null);
            task.execute(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                onCompleted(true);
            }else{
                Handler handler = new Handler();
                handler.postDelayed(() -> onCompleted(true), 1500);
            }
        }
    }

    private void fixBugs() {

        try {
            SharedPreferences hasFixedGoogleBug154855417 = getSharedPreferences("google_bug_154855417", Context.MODE_PRIVATE);
            if (!hasFixedGoogleBug154855417.contains("fixed")) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                File corruptedSavedClientParameters = new File(getFilesDir(), "SavedClientParameters.data.cs");
                File corruptedClientParametersData =
                        new File(
                                getFilesDir(),
                                "DATA_ServerControlledParametersManager.data."
                                        + getBaseContext().getPackageName());
                File corruptedClientParametersDataV1 =
                        new File(
                                getFilesDir(),
                                "DATA_ServerControlledParametersManager.data.v1."
                                        + getBaseContext().getPackageName());
                corruptedZoomTables.delete();
                corruptedSavedClientParameters.delete();
                corruptedClientParametersData.delete();
                corruptedClientParametersDataV1.delete();
                hasFixedGoogleBug154855417.edit().putBoolean("fixed", true).apply();
            }
        } catch (Exception e) {

        }
    }

    private void checkAllSubscriptions() {

        String[] prefs = new String[]{getResources().getString(R.string.notification_offers_title),
                getResources().getString(R.string.notification_bus_title),
                getResources().getString(R.string.notification_app_title),
                getResources().getString(R.string.notification_traffic_title),
                getResources().getString(R.string.notification_timetable_title)};

        String[] topics = new String[]{Constants.TOPIC_OFFERS, Constants.TOPIC_BUS, Constants.TOPIC_APP, Constants.TOPIC_TRAFFIC, Constants.TOPIC_TIMETABLE};

        for(int i =0; i < prefs.length; i++){
            String pref = prefs[i];

            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(pref,true)){
                // register onto a topic
                FirebaseMessaging.getInstance().subscribeToTopic(topics[i]);
            }else{
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topics[i]);
            }
        }
    }

    private void startOnBoarding(){
        Intent intent = new Intent(SplashScreenActivity.this, OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCompleted(Boolean bool) {

        Intent intent = new Intent(SplashScreenActivity.this, MainNavigationActivity.class);
        startActivity(intent);
        finish();

    }
}
