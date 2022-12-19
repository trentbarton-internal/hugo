package uk.co.trentbarton.hugo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.Map;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tools.Constants;


public class NotificationsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    String[] prefs;
    String[] topics;
    private final String TAG = this.getClass().getSimpleName();
    private Context context;

    public NotificationsFragment() {


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        try{
            if(prefs == null || topics == null){
                prefs = new String[]{getResources().getString(R.string.notification_offers_title),
                        getResources().getString(R.string.notification_bus_title),
                        getResources().getString(R.string.notification_app_title),
                        getResources().getString(R.string.notification_traffic_title),
                        getResources().getString(R.string.notification_timetable_title)};

                topics = new String[]{Constants.TOPIC_OFFERS, Constants.TOPIC_BUS, Constants.TOPIC_APP, Constants.TOPIC_TRAFFIC, Constants.TOPIC_TIMETABLE};
            }

            boolean preferenceToChange = false;
            int index = 0;

            for(int i = 0; i < prefs.length; i++){
                if(s.equals(prefs[i])){
                    preferenceToChange = true;
                    index = i;
                }
            }

            if(!preferenceToChange){
                return;
            }

            //Now we know that the preference which has changed is a boolean

            if(sharedPreferences.getBoolean(s,true)){
                // register onto a topic
                FirebaseMessaging.getInstance().subscribeToTopic(topics[index]);
            }else{
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topics[index]);
            }


            Map<String, ?> allPrefs = sharedPreferences.getAll();
            ArrayList<String> subscribed = new ArrayList<>();
            for(String key : allPrefs.keySet()){
                try{
                    if((Boolean) allPrefs.get(key)){
                        subscribed.add(key);
                    }
                }catch(Exception ignore){}
            }

            Context tempContext;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                tempContext = getContext();
            }else{
                tempContext = getActivity();
            }

            if(tempContext != null){
                context = tempContext;
            }

            if(context == null){
                Log.e(TAG, "Application context is null, see NotificationsFragment onSharedPreferenceChanged()");
            }else{
                HugoPreferences.setMessageTypesSubscribedTo(context, subscribed);
            }

        }catch(Exception ignore){

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_list);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }




}
