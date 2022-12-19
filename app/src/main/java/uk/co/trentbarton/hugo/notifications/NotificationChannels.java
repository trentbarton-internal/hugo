package uk.co.trentbarton.hugo.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import uk.co.trentbarton.hugo.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationChannels {

    public static String ALERTS_NOTIFICATION_CHANNEL = "uk.co.trentbarton.hugo.notifications.alerts";
    public static String ALIGHTING_NOTIFICATION_CHANNEL = "uk.co.trentbarton.hugo.notifications.alighting";
    public static String BUS_DUE_NOTIFICATION_CHANNEL = "uk.co.trentbarton.hugo.notifications.busdue";
    public static String DEFAULT_NOTIFICATION_CHANNEL = "uk.co.trentbarton.hugo.notifications.default";

    public static void setupNotificationChannels(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel alightingChannel = new NotificationChannel(ALIGHTING_NOTIFICATION_CHANNEL, "Alighting alarms", NotificationManager.IMPORTANCE_HIGH);
            alightingChannel.setDescription("This notification setting is used to notify you when your stop is approaching and you should press the bell to alight");
            alightingChannel.setLightColor(Color.RED);
            alightingChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            alightingChannel.setShowBadge(false);
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            builder.setUsage(AudioAttributes.USAGE_ALARM);
            alightingChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            alightingChannel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bus_bell), builder.build());
            alightingChannel.setVibrationPattern(new long[] {0,1000,500,1000,500,1000,500,1000});
            nm.createNotificationChannel(alightingChannel);

            NotificationChannel busDueChannel = new NotificationChannel(BUS_DUE_NOTIFICATION_CHANNEL, "Bus due alarms", NotificationManager.IMPORTANCE_HIGH);
            busDueChannel.setDescription("This notification setting is used to notify you when your bus is coming to your stop if you set an alarm");
            busDueChannel.setLightColor(Color.RED);
            busDueChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            busDueChannel.setShowBadge(false);
            busDueChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes.Builder builder2 = new AudioAttributes.Builder();
            builder2.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            builder2.setUsage(AudioAttributes.USAGE_ALARM);
            busDueChannel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm), builder.build());
            busDueChannel.setVibrationPattern(new long[] {0,1000,500,1000,500,1000,500,1000});
            nm.createNotificationChannel(busDueChannel);


            NotificationChannel alertsChannel = new NotificationChannel(ALERTS_NOTIFICATION_CHANNEL, "Location notifications", NotificationManager.IMPORTANCE_DEFAULT);
            alertsChannel.setDescription("This notification setting is used to identify when hugo is tracking your location and provide a visual indicator of how long is left of your current trip");
            alertsChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            AudioAttributes.Builder abuilder = new AudioAttributes.Builder();
            abuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            abuilder.setUsage(AudioAttributes.USAGE_NOTIFICATION);
            alertsChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), abuilder.build());
            alertsChannel.setShowBadge(true);
            alertsChannel.setVibrationPattern(new long[] {0,500, 500, 500});
            alertsChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(alertsChannel);

            NotificationChannel defaultChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, "Default notifications", NotificationManager.IMPORTANCE_DEFAULT);
            defaultChannel.setDescription("This notification setting is used as a default for all messages and alerts from hugo that are not defined by the other categories");
            defaultChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            AudioAttributes.Builder a1builder = new AudioAttributes.Builder();
            a1builder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            a1builder.setUsage(AudioAttributes.USAGE_NOTIFICATION);
            defaultChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), abuilder.build());
            defaultChannel.setShowBadge(true);
            defaultChannel.setVibrationPattern(new long[] {0,500, 500, 500});
            nm.createNotificationChannel(defaultChannel);

        }

    }




}
