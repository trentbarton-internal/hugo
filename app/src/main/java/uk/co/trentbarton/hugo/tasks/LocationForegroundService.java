package uk.co.trentbarton.hugo.tasks;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.SettingsActivity;
import uk.co.trentbarton.hugo.notifications.NotificationChannels;
import uk.co.trentbarton.hugo.tools.Tools;

public class LocationForegroundService extends Service implements Executor{

    private static final String TAG = LocationForegroundService.class.getSimpleName();
    public static boolean IS_SERVICE_RUNNING = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mDestination;
    private int mDistance;
    private NotificationCompat.Builder mBuilder;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private ArrayList<LatLng> stops;
    private boolean lastStopServed = false;
    private static final int SERVED_DISTANCE_RADIUS = 50; //50 METERS
    private static final int MIN_DISTANCE_TO_TRIGGER = 150; //150 METERS
    private static final int MIN_CHANGE_DISTANCE = 5; //150 METERS
    private static final double AVERAGE_SPEED_KMH = 19.0; //Average speed of bus routes
    private int mDistanceToLastStop = -1;
    private boolean isMovingCloser = true;
    private boolean userNotified = false;

    public static String CANCEL_FOREGROUND_ACTION = "uk.co.trentbarton.hugo.foreground.cancel";
    public static String START_FOREGROUND_ACTION = "uk.co.trentbarton.hugo.foreground.start";
    public static String STOP_FOREGROUND_ACTION = "uk.co.trentbarton.hugo.foreground.stop";
    public static int FOREGROUND_SERVICE_ID = 682;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent == null){
            try{
                Log.i(TAG, "Clicked Stop");
                IS_SERVICE_RUNNING = false;
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                stopForeground(true);
                stopSelf();
                Toast.makeText(this, "hugo is no longer monitoring your location", Toast.LENGTH_LONG).show();
            }catch(Exception ignore){
                //Do nothing
            }

            return START_NOT_STICKY;
        }

        if (intent.getExtras() != null) {

            if (intent.hasExtra("lat") && intent.hasExtra("lng")) {
                mDestination = new LatLng(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lng", 0));
            } else {
                Log.i(TAG, "intent did not have extras passed in");
            }

            if(intent.hasExtra("polyLine")){
                stops = new ArrayList<>();
                stops.addAll(PolyUtil.decode(intent.getStringExtra("polyLine")));
            }
        }

        if (intent.getAction().equals(START_FOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            if (startMonitoring()) {
                //If this process fails to work then we won't have permission to complete the rest
                showNotification();
                Toast.makeText(this, "hugo is now monitoring your location", Toast.LENGTH_LONG).show();
                IS_SERVICE_RUNNING = true;
            }

        } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
            Log.i(TAG, "Clicked Stop");
            IS_SERVICE_RUNNING = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopForeground(true);
            stopSelf();
            Toast.makeText(this, "hugo is no longer monitoring your location", Toast.LENGTH_LONG).show();

        } else if (intent.getAction().equals(CANCEL_FOREGROUND_ACTION)) {
            Log.i(TAG, "Clicked Cancel");
            IS_SERVICE_RUNNING = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopForeground(true);
            stopSelf();
            Toast.makeText(this, "hugo is no longer monitoring your location", Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }

    private boolean startMonitoring() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location services are disabled so this process can't work", Toast.LENGTH_LONG).show();
            return false;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                mCurrentLocation = location;
                Location loc = new Location(location);
                loc.setLatitude(mDestination.latitude);
                loc.setLongitude(mDestination.longitude);
                mDistance = (int) location.distanceTo(loc);
            }
        });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(6000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                float lowestAccuracy = 0;
                Location bestLocation = null;

                for (Location location : locationResult.getLocations()) {

                    if(lowestAccuracy == 0 || location.getAccuracy() < lowestAccuracy){
                        lowestAccuracy = location.getAccuracy();
                        bestLocation = location;
                    }
                }

                if(bestLocation != null){
                    mCurrentLocation = bestLocation;
                    mDistance = (int) Tools.distance(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), mDestination);
                    if(!userNotified){
                        if(!checkIfWeShouldNotify()){
                            updateNotification();
                        }
                    }else{
                        try{
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            stopForeground(true);
                            stopSelf();
                        }catch(Exception ignore){}
                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        return true;

    }

    private boolean checkIfWeShouldNotify() {

        if(mDistance != 0 && mDistance > 1000){
            //if distance is not zero and we're not within 1000m then it's not going to need a notification
            return false;
        }

        if(hasPassedLastStop() || mDistance <= MIN_DISTANCE_TO_TRIGGER){
            //Show an alarm of some kind
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_large);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannels.ALIGHTING_NOTIFICATION_CHANNEL)
                    .setContentTitle("It's time to get off the bus")
                    .setTicker("hugo")
                    .setContentText("Please press the bell to tell the driver you would like to alight")
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.arrows)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setLights(Color.RED, 300, 300)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    .setVibrate((new long[] { 0, 1000, 1000, 1000, 1000,1000,1000,1000}));
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(23, builder.build());

            IS_SERVICE_RUNNING = false;
            userNotified = true;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopForeground(true);
            stopSelf();
            Toast.makeText(this, "hugo is no longer monitoring your location", Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private boolean hasPassedLastStop(){

        if(stops == null || stops.size() < 2){
            return false;
        }

        //Get the next to last stop
        if(!lastStopServed){ //run this if we haven't recorded a visit to this stop yet
            LatLng lastStopLocation = stops.get(stops.size() - 2);
            mDistanceToLastStop = (int) Tools.distance(lastStopLocation, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            if(mDistanceToLastStop < SERVED_DISTANCE_RADIUS){
                lastStopServed = true;
            }
            //Here if we haven't yet served the stop how can we be moving away from it
            return false;

        }

        LatLng lastStopLocation = stops.get(stops.size() - 2);
        int newDistanceToLastStop = (int) Tools.distance(lastStopLocation, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));


        int difference = Math.abs(newDistanceToLastStop - mDistanceToLastStop);

        if(newDistanceToLastStop < mDistanceToLastStop){
            //Moving closer
            //change movingCloserVariable if distance > min_change
            if(difference > MIN_CHANGE_DISTANCE){
                isMovingCloser = true;
                mDistanceToLastStop = newDistanceToLastStop;
            }
        }else{
            //moving further away
            if(difference > MIN_CHANGE_DISTANCE){
                if(!isMovingCloser){
                    //We have 2 consecutive moving further aways now with a change equal to 10m or more, if we have already served the stop then return true
                    if(lastStopServed){
                        return true;
                    }
                    isMovingCloser = false;
                    mDistanceToLastStop = newDistanceToLastStop;
                }else{
                    isMovingCloser = false;
                    mDistanceToLastStop = newDistanceToLastStop;
                }
            }
        }

        return false;

    }

    private void updateNotification(){
        String message = "";
        if(mDistance != 0){

            int mins = (int)(((mDistance / 1000.0) / AVERAGE_SPEED_KMH) * 60.0);

            if(lastStopServed){
                message = "You are currently approaching the last stop before your stop, please get ready";
                mBuilder.setVibrate(new long[]{0,1000,1000,1000,1000,1000,1000});
            }else{
                if(mDistance >= 1000){
                    message = String.format(Locale.ENGLISH,"you are %.1fkm away, estimated %d mins", (mDistance / 1000.0f), mins);
                }else{
                    message = String.format(Locale.ENGLISH,"you are %dm away, estimated %d mins", mDistance, mins);
                }
            }
        }
        mBuilder.setContentText(message);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(FOREGROUND_SERVICE_ID, mBuilder.build());
    }

    private void showNotification() {

        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        notificationIntent.setAction(START_FOREGROUND_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent cancelIntent = new Intent(this, LocationForegroundService.class);
        cancelIntent.setAction(CANCEL_FOREGROUND_ACTION);
        PendingIntent pCancelIntent = PendingIntent.getService(this, 0, cancelIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_large);

        NotificationChannels.setupNotificationChannels(getBaseContext());
        mBuilder = new NotificationCompat.Builder(this, NotificationChannels.ALERTS_NOTIFICATION_CHANNEL)
                .setContentTitle("hugo is monitoring your location")
                .setTicker("hugo")
                .setContentText("hugo is working out where you are...")
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.arrows)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Cancel", pCancelIntent);

        startForeground(FOREGROUND_SERVICE_ID, mBuilder.build());
        updateNotification();

    }

    @Override
    public void execute(@NonNull Runnable command) {

    }
}
