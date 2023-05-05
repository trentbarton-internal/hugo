package uk.co.trentbarton.hugo.tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import uk.co.trentbarton.hugo.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Constants {

    //public static final String HUGO_API_URL = "https://apps.trentbarton.co.uk/Hugo/version3/index.php";
    //TODO - ONLY USE FOR TESTING
    public static final String HUGO_API_URL = "https://apps.trentbarton.co.uk/Hugo/version5/index.php"; //Testing only
    public static final String OPERATING_SYSTEM = "Android";
    public static final LatLng SOUTH_WEST_MAP_CORNER = new LatLng(52.597118,-1.735656);
    public static final LatLng NORTH_EAST_MAP_CORNER = new LatLng(53.275800,-0.794952);

    public static final String TOPIC_OFFERS = "Offers";
    public static final String TOPIC_APP = "App";
    public static final String TOPIC_BUS = "Bus";
    public static final String TOPIC_TRAFFIC = "Traffic";
    public static final String TOPIC_TIMETABLE = "Timetable";
    public static final String TOPIC_TEST = "test";





}
