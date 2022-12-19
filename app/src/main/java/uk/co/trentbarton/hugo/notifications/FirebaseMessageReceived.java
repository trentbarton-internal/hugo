package uk.co.trentbarton.hugo.notifications;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.SendPushTokenParams;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;

public class FirebaseMessageReceived extends FirebaseMessagingService {

    private final String TAG = this.getClass().getSimpleName();
    public static final String REQUEST_CANCEL_ALARM = "Request_Cancel_Alarm";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sendRegistrationToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData() != null){

            String type = remoteMessage.getData().get("messageType");

            if(type != null && type.equalsIgnoreCase("alarm")){
                cancelAlarm();
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_large);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NotificationChannels.BUS_DUE_NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.alarm_green)
                        .setContentTitle(remoteMessage.getData().get("title"))
                        .setContentText(remoteMessage.getData().get("message"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(false)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("message")))
                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setLights(Color.RED, 300, 300)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .setVibrate(new long[]{1000,500,1000,500,1000,500,1000,500,1000});
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(35, mBuilder.build());
            }
        }
    }

    private void cancelAlarm() {
        HugoPreferences.setActiveAlarm(this, null);
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
        Intent intent = new Intent(REQUEST_CANCEL_ALARM);
        broadcaster.sendBroadcast(intent);
    }

    public void sendRegistrationToServer(String token) {

        Log.d(TAG, "Server token:" + token);
        HugoPreferences.setPushToken(this,token);
        HugoPreferences.setPushSent(FirebaseMessageReceived.this, false);
        SendPushTokenParams params = new SendPushTokenParams(this);
        params.setPushToken(token);
        DataRequestTask task = new DataRequestTask(params);
        task.setOnTaskCompletedListener(result -> {
            if(result){
                Log.d(TAG, "Token successfully sent to server");
                HugoPreferences.setPushSent(FirebaseMessageReceived.this, true);
            }else{
                Log.d(TAG, "Token update to hugo servers failed because " + task.getErrorMessage());
            }
        });
        task.execute(this);

    }
}
