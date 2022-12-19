package uk.co.trentbarton.hugo.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.trentbarton.hugo.notifications.NotificationChannels;

public class Tools {

    public static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    public static double distance (LatLng pos1, LatLng pos2)
    {
        if(pos1 == null || pos2 == null){
            return -1;
        }

        double lat_a = pos1.latitude;
        double lng_a = pos1.longitude;
        double lat_b = pos2.latitude;
        double lng_b = pos2.longitude;

        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }

    public static boolean areNotificationsBlocked(Context context){

        if(!NotificationManagerCompat.from(context).areNotificationsEnabled()){
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isChannelBlocked(NotificationChannels.BUS_DUE_NOTIFICATION_CHANNEL, context)) {
            return true;
        }

        return false;

    }

    @RequiresApi(26)
    private static boolean isChannelBlocked(String channelId, Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationChannel channel = manager.getNotificationChannel(channelId);

        return channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE;
    }

    public static void openNotificationSettings(Context context){

        if(context == null) return;

        try{
            if(!NotificationManagerCompat.from(context).areNotificationsEnabled()){
                //Open general notification info
                openStandardNotificationSettings(context);
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                openChannelSettings(context);
            }else{
                Toast.makeText(context, "Failed to open up settings, please navigate to settings manually", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(context, "Failed to open up settings, please navigate to settings manually", Toast.LENGTH_LONG).show();
        }

    }

    @RequiresApi(26)
    private static void openChannelSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.BUS_DUE_NOTIFICATION_CHANNEL);
        context.startActivity(intent);
    }

    private static void openStandardNotificationSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }


}
